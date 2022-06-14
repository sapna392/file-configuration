package core.com.file.management.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang3.StringUtils;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import core.com.file.management.common.ErrorCode;
import core.com.file.management.common.FileManagementConstant;
import core.com.file.management.entity.BulkUploadFileEntity;
import core.com.file.management.entity.FileConfigurationEntity;
import core.com.file.management.exception.VendorBulkUploadException;
import core.com.file.management.model.BulkUploadFileResponse;
import core.com.file.management.model.BulkUploadFileRest;
import core.com.file.management.model.ResponseMetadata;
import core.com.file.management.model.VendorTxnInvoiceRest;
import core.com.file.management.repo.BulkUploadFileRepo;
import core.com.file.management.repo.FileConfigurationRepo;
import core.com.file.management.service.VendorBulkUploadFileService;
import core.com.file.management.util.FileManagementUtil;
import core.com.file.management.validation.VendorTxnInvoiceValidator;

@Service
public class VendorBulkUploadFileServiceImpl implements VendorBulkUploadFileService {

	@Value("${core.scfu.max.file.size}")
	private Integer MAX_FILE_SIZE;

	@Autowired
	private FileManagementUtil fileManagementUtil;

	@Autowired
	private VendorTxnInvoiceValidator vendorTxnInvoiceValidator;

	@Autowired
	private BulkUploadFileRepo bulkUploadFileRepo;

	@Autowired
	private FileConfigurationRepo fileConfigurationRepo;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private Mapper mapper;

	@Override
	public String upload(MultipartFile file, String imCode) throws VendorBulkUploadException {

		try {
			if (!(FileManagementConstant.EXCEL_MIME_TYPE.equals(file.getContentType())
					|| FileManagementConstant.CSV_MIME_TYPE.equals(file.getContentType())
					|| FileManagementConstant.TXT_MIME_TYPE.equals(file.getContentType()))) {
				throw new VendorBulkUploadException(ErrorCode.INVALID_FILE_TYPE);
			}
			if (file.isEmpty()) {
				throw new VendorBulkUploadException(ErrorCode.EMPTY_FILE_CONTENT);
			}
			FileConfigurationEntity fileConfigurationEntity = fileConfigurationRepo.getFileConfiguration(imCode, null);
			if (fileConfigurationEntity == null) {
				throw new VendorBulkUploadException(ErrorCode.FILE_CONFIG_DOESNOT_EXISTS);
			}
			if (FileManagementConstant.EXCEL_MIME_TYPE.equals(file.getContentType())
					|| FileManagementConstant.CSV_MIME_TYPE.equals(file.getContentType())
							&& FileManagementConstant.FIXED.equals(fileConfigurationEntity.getFileStructure())) {
				throw new VendorBulkUploadException(ErrorCode.FILE_CONFIG_DOESNOT_EXISTS);
			}

			List<String> contentList = null;
			int count = 0;
			double amount = 0.00;
			if (FileManagementConstant.EXCEL_MIME_TYPE.equals(file.getContentType())) {
				contentList = fileManagementUtil.readFromExcelWorkbook(file.getInputStream());
			} else if (FileManagementConstant.TXT_MIME_TYPE.equals(file.getContentType())
					|| FileManagementConstant.CSV_MIME_TYPE.equals(file.getContentType())) {
				contentList = Arrays.asList(
						new String(file.getInputStream().readAllBytes()).split(FileManagementConstant.LINE_DELIMITER));
			}

			contentList = contentList.stream().filter(cl -> StringUtils.isNotEmpty(cl)).collect(Collectors.toList());
			VendorTxnInvoiceRest vendorTxnInvcRest = null;
			Map<String, String> configMap = objectMapper.convertValue(fileConfigurationEntity, Map.class);
			if (FileManagementConstant.TXT_MIME_TYPE.equals(file.getContentType())) {
				for (String content : contentList) {
					Map<String, String> contentMap = new HashMap<>();
					if (FileManagementConstant.DELIMITER.equals(fileConfigurationEntity.getFileStructure())) {
						if (!content.contains(fileConfigurationEntity.getFileDelimiter())) {
							throw new VendorBulkUploadException(ErrorCode.FILE_CONFIG_DOESNOT_MATCH);
						} else {
							contentMap = mapBulkUploadFields(content, fileConfigurationEntity, configMap);
						}
						vendorTxnInvcRest = objectMapper.convertValue(contentMap, VendorTxnInvoiceRest.class);
						vendorTxnInvoiceValidator.validateUploadedFile(vendorTxnInvcRest);
						count++;
						amount += vendorTxnInvcRest.getInvoiceAmount();
					} else if (FileManagementConstant.FIXED.equals(fileConfigurationEntity.getFileStructure())) {
						for (Map.Entry<String, String> entry : configMap.entrySet()) {
							if (FileManagementConstant.ADDITIONAL_FIELD.contains(entry.getKey())
									&& entry.getValue() != null) {
								String[] pos = entry.getValue().split(FileManagementConstant.PIPE_DELIMITER)[1]
										.split(FileManagementConstant.COMMA);
								contentMap.put(entry.getKey(),
										content.substring(Integer.parseInt(pos[0]), Integer.parseInt(pos[1]) + 1));
							}
							String[] pos = entry.getValue().split(FileManagementConstant.COMMA);
							contentMap.put(entry.getKey(),
									content.substring(Integer.parseInt(pos[0]), Integer.parseInt(pos[1]) + 1));
							vendorTxnInvcRest = objectMapper.convertValue(contentMap, VendorTxnInvoiceRest.class);
							vendorTxnInvoiceValidator.validateUploadedFile(vendorTxnInvcRest);
							count++;
							amount += vendorTxnInvcRest.getInvoiceAmount();
						}
					}
				}
			} else if (FileManagementConstant.EXCEL_MIME_TYPE.equals(file.getContentType())
					|| FileManagementConstant.CSV_MIME_TYPE.equals(file.getContentType())) {
				for (String content : contentList) {
					Map<String, String> contentMap = mapBulkUploadFields(content, fileConfigurationEntity, configMap);
					vendorTxnInvcRest = objectMapper.convertValue(contentMap, VendorTxnInvoiceRest.class);
					vendorTxnInvoiceValidator.validateUploadedFile(vendorTxnInvcRest);
					count++;
					amount += vendorTxnInvcRest.getInvoiceAmount();
				}
			}

			String contentHash = fileManagementUtil.getContentHash(file.getName());
			String filePath = fileManagementUtil.getFilePath(contentHash, file.getName());
			File savedFile = new File(filePath);
			file.transferTo(savedFile);
			/*
			 * if (FileManagementConstant.EXCEL_MIME_TYPE.equals(file.getContentType())) {
			 * workbook = createExcelWorkbook(decodedString);
			 * workbook.write(fileOutputStream); } else {
			 * fileOutputStream.write(decodedString.getBytes()); }
			 */
			BulkUploadFileEntity bulkUploadFileEntity = new BulkUploadFileEntity();
			bulkUploadFileEntity.setImCode(imCode);
			bulkUploadFileEntity.setName(file.getName());
			bulkUploadFileEntity.setStatus(FileManagementConstant.STATUS_PENDING);
			bulkUploadFileEntity.setType(file.getContentType());
			bulkUploadFileEntity.setInvoiceCount(count);
			bulkUploadFileEntity.setTotalAmount(amount);
			bulkUploadFileEntity.setHash(contentHash);
			String bulkFileGuid = fileManagementUtil.getGuid(FileManagementConstant.BULK_UPLOAD);
			bulkUploadFileEntity.setGuid(bulkFileGuid);
			bulkUploadFileEntity.setCreated(new Date());
			bulkUploadFileEntity.setUpdated(new Date());
			bulkUploadFileRepo.save(bulkUploadFileEntity);

		} catch (IOException | NoSuchAlgorithmException | VendorBulkUploadException exp) {
			String message = null;
			if (StringUtils.isNotBlank(exp.getMessage())) {
				message = exp.getMessage();
			} else {
				message = ErrorCode.FILE_PROCESSING_ERROR;
			}
			throw new VendorBulkUploadException(message);
		}
		return FileManagementConstant.FILE_UPLOADED_SUCCESS;
	}

	@Override
	public BulkUploadFileResponse getUploadFileDetails(Pageable pageable, String status, String imCode) {

		Page<BulkUploadFileEntity> bulkUploadPage = null;
		if (status == null) {
			bulkUploadPage = bulkUploadFileRepo.findAll(pageable);
		} else {
			bulkUploadPage = bulkUploadFileRepo.findByStatusAndImCode(imCode, status, pageable);
		}

		List<BulkUploadFileRest> uploadFileRestList = bulkUploadPage.getContent().stream()
				.map(bup -> mapper.map(bup, BulkUploadFileRest.class)).collect(Collectors.toList());

		ResponseMetadata metadata = new ResponseMetadata();
		metadata.setElements(bulkUploadPage.getTotalElements());
		metadata.setTotalPages(bulkUploadPage.getTotalPages());
		metadata.setSize(bulkUploadPage.getSize());
		metadata.setPage(bulkUploadPage.getNumber());
		BulkUploadFileResponse uploadFileResponse = new BulkUploadFileResponse();
		uploadFileResponse.setMetadata(metadata);
		uploadFileResponse.setData(uploadFileRestList);

		return uploadFileResponse;
	}

	@Override
	public MultipartFile getUploadFileById(Long id, String userId, String serType) throws VendorBulkUploadException {

		BulkUploadFileEntity uploadFileEntity = bulkUploadFileRepo.getFileById(id, userId);
		String filePath = fileManagementUtil.getFilePath(uploadFileEntity.getHash(), uploadFileEntity.getName());
		MultipartFile multipartFile = null;
		try {
			File file = new File(filePath);
			DiskFileItem fileItem = new DiskFileItem("file", uploadFileEntity.getType(), false, file.getName(),
					(int) file.length(), file.getParentFile());
			fileItem.getOutputStream();
			multipartFile = new CommonsMultipartFile(fileItem);
		} catch (IOException e) {
			throw new VendorBulkUploadException(ErrorCode.FILE_DOWNLOADING_ERROR);
		}
		
		/*InputStream inputStream = null;
		BulkUploadFileEntity uploadFileEntity = bulkUploadFileRepo.getById(Long.valueOf(id));
		BulkUploadFileRest uploadFileRest = mapper.map(uploadFileEntity, BulkUploadFileRest.class);
		if (FileManagementConstant.XLS_FILE.equals(uploadFileRest.getType())) {
			String filePath = fileManagementUtil.getFilePath(uploadFileEntity.getHash(), uploadFileRest.getName(),
					uploadFileRest.getType());
			try {
				inputStream = new InputStream(filePath);
				String content = readFromExcelWorkbook(inputStream);
				uploadFileRest.setContent(content);
			} catch (FileNotFoundException e) {
				throw new FileConfigurationException(ErrorCode.FILE_PROCESSING_ERROR);
			} catch (IOException e) {
				throw new FileConfigurationException(ErrorCode.FILE_PROCESSING_ERROR);
			} finally {
				try {
					inputStream.close();
				} catch (IOException e) {
					throw new FileConfigurationException(ErrorCode.FILE_PROCESSING_ERROR);
				}
			}*/
		return multipartFile;
	}
	
	@Override
	public InputStream download(String userId, String userType, String mediaType) throws VendorBulkUploadException {

		FileConfigurationEntity fileConfigurationEntity = fileConfigurationRepo.getFieldPostion(userId, userType);
		if (fileConfigurationEntity != null) {
			Map<String, String> confifMap = objectMapper.convertValue(fileConfigurationEntity, Map.class);
			Map<String, Integer> filteredConfigMap = new HashMap<>();
			for (Map.Entry<String, String> entry : confifMap.entrySet()) {
				if (StringUtils.isNotBlank(entry.getValue())
						&& !FileManagementConstant.FILE_DELIMITER.equals(entry.getKey())) {
					if (entry.getValue().contains(FileManagementConstant.PIPE)) {
						String[] strArr = entry.getValue().split(FileManagementConstant.PIPE_DELIMITER);
						filteredConfigMap.put(strArr[0], Integer.parseInt(strArr[1]));
					} else {
						filteredConfigMap.put(entry.getKey(), Integer.parseInt(entry.getValue()));
					}
				}
			}
			List<String> sortedConfigMapKeys = filteredConfigMap.entrySet().stream()
					.sorted(Map.Entry.comparingByValue())
					.collect(Collectors.mapping(scm -> scm.getKey(), Collectors.toList()));
			ByteArrayInputStream byteArrayInputStream = null;
			try {
				if (FileManagementConstant.CSV_MIME_TYPE.equals(mediaType)) {
					byteArrayInputStream = fileManagementUtil.writeToCsvFile(sortedConfigMapKeys);
				} else if (FileManagementConstant.TXT_MIME_TYPE.equals(mediaType)) {
					byteArrayInputStream = fileManagementUtil.writeToTxtFile(sortedConfigMapKeys,
							confifMap.get(FileManagementConstant.FILE_DELIMITER));
				} else if (FileManagementConstant.XLS_MIME_TYPE.equals(mediaType)) {
					byteArrayInputStream = fileManagementUtil.writeToXlsFile(sortedConfigMapKeys);
				}
			} catch (IOException e) {
				throw new VendorBulkUploadException(e.getMessage());
			}
			return byteArrayInputStream;
		} else {
			throw new VendorBulkUploadException(ErrorCode.FILE_CONFIG_DOESNOT_EXISTS);
		}
	}

	/*private HSSFWorkbook createExcelWorkbook(String uploadDecodedContent) {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet();
		List<String> contentList = Arrays.asList(uploadDecodedContent.split(FileManagementConstant.LINE_DELIMITER));
		int rowNum = 0;
		contentList.forEach(cl -> {
			HSSFRow row = sheet.createRow(Math.abs(~rowNum)); // increment without addition since addition is blocked
			List<String> clList = Arrays.asList(cl.split(FileManagementConstant.COMMA));
			int cellNum = 0;
			clList.forEach(cll -> {
				HSSFCell cell = row.createCell(Math.abs(~cellNum));
				cell.setCellValue((String) cll);
			});
		});
		return workbook;
	}*/

	private Map<String, String> mapBulkUploadFields(String content, FileConfigurationEntity fileConfigurationEntity,
			Map<String, String> configMap) {
		Map<String, String> contentMap = new HashMap<>();
		String[] invoiceDetails = content.split(fileConfigurationEntity.getFileDelimiter());
		for (Map.Entry<String, String> entry : configMap.entrySet()) {
			if (FileManagementConstant.ADDITIONAL_FIELD.contains(entry.getKey()) && entry.getValue() != null) {
				String[] additionalFields = entry.getValue().split(FileManagementConstant.PIPE_DELIMITER);
				contentMap.put(entry.getKey(), invoiceDetails[Integer.parseInt(additionalFields[1])]);
			} else {
				contentMap.put(entry.getKey(), invoiceDetails[Integer.parseInt(entry.getValue())]);
			}
		}
		return contentMap;
	}

}
