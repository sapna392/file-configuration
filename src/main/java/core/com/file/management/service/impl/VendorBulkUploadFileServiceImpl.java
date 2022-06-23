package core.com.file.management.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang3.StringUtils;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
import core.com.file.management.model.VendorBulkUploadRest;
import core.com.file.management.repo.BulkUploadFileRepo;
import core.com.file.management.repo.FileConfigurationRepo;
import core.com.file.management.service.VendorBulkUploadFileService;
import core.com.file.management.util.FileConfigurationUtil;
import core.com.file.management.validator.VendorBulkUploadValidator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class VendorBulkUploadFileServiceImpl implements VendorBulkUploadFileService {

	@Value("${core.scfu.max.file.size}")
	private Integer MAX_FILE_SIZE;

	@Autowired
	private FileConfigurationUtil fileManagementUtil;

	@Autowired
	private VendorBulkUploadValidator vendorBulkUploadValidator;

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

		log.info("Entering upload of {}", this.getClass().getSimpleName());
		
		try {
			List<FileConfigurationEntity> fileConfigurationEntityList = fileConfigurationRepo
					.getFileConfiguration(imCode);
			if (CollectionUtils.isEmpty(fileConfigurationEntityList)) {
				throw new VendorBulkUploadException(ErrorCode.FILE_CONFIG_DOESNOT_EXISTS);
			}
			FileConfigurationEntity fileConfigurationEntity = fileConfigurationEntityList.get(0);
			vendorBulkUploadValidator.validateUploadedFile(file, fileConfigurationEntity.getFileStructure());

			List<String> contentList = null;
			int count = 0;
			double amount = 0.00;
			if (FileManagementConstant.XLS_MIME_TYPE.equals(file.getContentType())) {
				contentList = fileManagementUtil.readFromExcelWorkbook(file.getInputStream());
			} else if (FileManagementConstant.TXT_MIME_TYPE.equals(file.getContentType())
					|| FileManagementConstant.CSV_MIME_TYPE.equals(file.getContentType())) {
				contentList = Arrays.asList(
						new String(file.getInputStream().readAllBytes()).split(FileManagementConstant.LINE_DELIMITER));
			}

			contentList = contentList.stream().filter(cl -> StringUtils.isNotEmpty(cl)).collect(Collectors.toList());
			VendorBulkUploadRest vendorBulkUploadRest = null;
			Map<String, String> configMap = objectMapper.convertValue(fileConfigurationEntity, Map.class);
			configMap = configMap.entrySet().stream().filter(cm -> StringUtils.isNotBlank(cm.getValue()))
					.collect(Collectors.toMap(cm -> cm.getKey(), cm -> cm.getValue()));
			configMap.remove(FileManagementConstant.FILE_CONFIG_DELIMITER);
			configMap.remove(FileManagementConstant.FILE_CONFIG_STRUCTURE);
			if (FileManagementConstant.TXT_MIME_TYPE.equals(file.getContentType())) {
				for (String content : contentList) {
					Map<String, String> contentMap = new HashMap<>();
					if (FileManagementConstant.FILE_CONFIG_DELIMITER
							.equals(fileConfigurationEntity.getFileStructure())) {
						if (!content.contains(fileConfigurationEntity.getFileDelimiter())) {
							throw new VendorBulkUploadException(ErrorCode.FILE_CONFIG_DOESNOT_MATCH);
						} else {
							contentMap = mapBulkUploadFields(content, fileConfigurationEntity.getFileDelimiter(),
									configMap);
						}
						vendorBulkUploadRest = objectMapper.convertValue(contentMap, VendorBulkUploadRest.class);
						vendorBulkUploadValidator.validateInvoiceDetails(vendorBulkUploadRest);
						count++;
						amount += vendorBulkUploadRest.getInvoiceAmount();
					} else if (FileManagementConstant.FIXED.equals(fileConfigurationEntity.getFileStructure())) {
						for (Map.Entry<String, String> entry : configMap.entrySet()) {
							String[] pos = null;
							if (FileManagementConstant.ADDITIONAL_DB_FIELDS.contains(entry.getKey())
									&& entry.getValue() != null) {
								pos = entry.getValue().split(FileManagementConstant.PIPE_DELIMITER)[1]
										.split(FileManagementConstant.COMMA);
							} else {
								pos = entry.getValue().split(FileManagementConstant.COMMA);
							}
							contentMap.put(entry.getKey(),
									content.substring(Integer.parseInt(pos[0]), Integer.parseInt(pos[1]) + 1));
							vendorBulkUploadRest = objectMapper.convertValue(contentMap, VendorBulkUploadRest.class);
							vendorBulkUploadValidator.validateInvoiceDetails(vendorBulkUploadRest);
							count++;
							amount += vendorBulkUploadRest.getInvoiceAmount();
						}
					}
				}
			} else if (FileManagementConstant.XLS_MIME_TYPE.equals(file.getContentType())
					|| FileManagementConstant.CSV_MIME_TYPE.equals(file.getContentType())) {
				List<String> configHeader = configMap.entrySet().stream().map(cm -> {
					if (FileManagementConstant.ADDITIONAL_DB_FIELDS.contains(cm.getKey())) {
						return cm.getValue().split(FileManagementConstant.PIPE_DELIMITER)[0];
					} else {
						return cm.getKey();
					}
				}).collect(Collectors.toList());

				String headers = String.join(FileManagementConstant.COMMA, configHeader);
				if(headers.equalsIgnoreCase(contentList.get(0))) {
					throw new VendorBulkUploadException(ErrorCode.FILE_CONFIG_DOESNOT_MATCH);
				}
				contentList.remove(0);
				for (String content : contentList) {
					Map<String, String> contentMap = mapBulkUploadFields(content, FileManagementConstant.COMMA,
							configMap);
					vendorBulkUploadRest = objectMapper.convertValue(contentMap, VendorBulkUploadRest.class);
					vendorBulkUploadValidator.validateInvoiceDetails(vendorBulkUploadRest);
					count++;
					amount += vendorBulkUploadRest.getInvoiceAmount();
				}
			}

			String contentHash = fileManagementUtil.getContentHash(file.getOriginalFilename());
			String filePath = fileManagementUtil.getFilePath(contentHash);
			// Saving file to local starts
			File savedFile = new File(filePath);
			if(!savedFile.exists())
				savedFile.mkdirs();
			savedFile = new File(filePath, file.getOriginalFilename());
			savedFile.createNewFile();
			try (OutputStream outStream = new FileOutputStream(savedFile)) {
			    outStream.write(file.getInputStream().readAllBytes());
			}
			// Saving file to local ends
			/*
			 * if (FileManagementConstant.EXCEL_MIME_TYPE.equals(file.getContentType())) {
			 * workbook = createExcelWorkbook(decodedString);
			 * workbook.write(fileOutputStream); } else {
			 * fileOutputStream.write(decodedString.getBytes()); }
			 */
			BulkUploadFileEntity bulkUploadFileEntity = new BulkUploadFileEntity();
			bulkUploadFileEntity.setImCode(imCode);
			bulkUploadFileEntity.setName(file.getOriginalFilename());
			bulkUploadFileEntity.setStatus(FileManagementConstant.STATUS_PENDING);
			bulkUploadFileEntity.setType(file.getContentType());
			bulkUploadFileEntity.setInvoiceCount(count);
			bulkUploadFileEntity.setTotalAmount(amount);
			bulkUploadFileEntity.setHash(contentHash);
			String bulkFileGuid = fileManagementUtil.getGuid(FileManagementConstant.BULK_UPLOAD);
			bulkUploadFileEntity.setGuid(bulkFileGuid);
			bulkUploadFileEntity.setCreated(new Date());
			bulkUploadFileEntity.setCreatedBy(imCode);
			bulkUploadFileEntity.setUpdated(new Date());
			bulkUploadFileEntity.setUpdatedBy(imCode);
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
		
		log.info("Exiting upload of {}", this.getClass().getSimpleName());
		return FileManagementConstant.FILE_UPLOADED_SUCCESS;
	}

	@Override
	public BulkUploadFileResponse getUploadFileDetails(Pageable pageable, String status, String imCode) {

		log.info("Entering getUploadFileDetails of {}", this.getClass().getSimpleName());
		
		BulkUploadFileResponse uploadFileResponse = new BulkUploadFileResponse();
		List<BulkUploadFileRest> uploadFileRestList = null;
		Page<BulkUploadFileEntity> bulkUploadPage = null;
		if (status == null) {
			bulkUploadPage = bulkUploadFileRepo.findByImCode(imCode, pageable);
		} else {
			bulkUploadPage = bulkUploadFileRepo.findByStatusAndImCode(imCode, status, pageable);
		}

		if(CollectionUtils.isNotEmpty(bulkUploadPage.getContent())) {
			uploadFileRestList = bulkUploadPage.getContent().stream()
					.map(bup -> mapper.map(bup, BulkUploadFileRest.class)).collect(Collectors.toList());
			uploadFileResponse.setStatus_msg(FileManagementConstant.FILE_DTLS_FETCH_SUCCESS);
		} else {
			uploadFileResponse.setStatus_msg(FileManagementConstant.FILE_CONFIG_DOESNOT_EXISTS);
		}

		ResponseMetadata metadata = new ResponseMetadata();
		metadata.setElements(bulkUploadPage.getTotalElements());
		metadata.setTotalPages(bulkUploadPage.getTotalPages());
		metadata.setSize(bulkUploadPage.getSize());
		metadata.setPage(bulkUploadPage.getNumber());
		uploadFileResponse.setMetadata(metadata);
		uploadFileResponse.setData(uploadFileRestList);
		uploadFileResponse.setStatus(FileManagementConstant.SUCCESS);
		uploadFileResponse.setStatus_code(String.valueOf(HttpStatus.OK.value()));
		

		log.info("Exiting getUploadFileDetails of {}", this.getClass().getSimpleName());
		return uploadFileResponse;
	}

	@Override
	public MultipartFile getUploadFileById(Long id, String userId, String serType) throws VendorBulkUploadException {

		log.info("Entering getUploadFileById of {}", this.getClass().getSimpleName());
		
		BulkUploadFileEntity uploadFileEntity = bulkUploadFileRepo.getFileById(id, userId);
		String filePath = fileManagementUtil.getFilePath(uploadFileEntity.getHash());
		MultipartFile multipartFile = null;
		try {
			File file = new File(filePath, uploadFileEntity.getName());
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
		
		log.info("Exiting getUploadFileById of {}", this.getClass().getSimpleName());
		return multipartFile;
	}
	
	@Override
	public InputStream download(String userId, String userType, String mediaType) throws VendorBulkUploadException {

		log.info("Entering download of {}", this.getClass().getSimpleName());
		
		List<FileConfigurationEntity> fileConfigurationEntityList = fileConfigurationRepo.getFileConfiguration(userId);
		if (CollectionUtils.isNotEmpty(fileConfigurationEntityList)) {
			Map<String, String> confifMap = objectMapper.convertValue(fileConfigurationEntityList.get(0), Map.class);
			Map<String, Integer> filteredConfigMap = new HashMap<>();
			for (Map.Entry<String, String> entry : confifMap.entrySet()) {
				if (StringUtils.isNotBlank(entry.getValue())
						&& !FileManagementConstant.FILE_CONFIG_DELIMITER.equals(entry.getKey())) {
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
							confifMap.get(FileManagementConstant.FILE_CONFIG_DELIMITER));
				} else if (FileManagementConstant.XLS_MIME_TYPE.equals(mediaType)) {
					byteArrayInputStream = fileManagementUtil.writeToXlsFile(sortedConfigMapKeys);
				}
			} catch (IOException e) {
				throw new VendorBulkUploadException(e.getMessage());
			}
			log.info("Exiting download of {}", this.getClass().getSimpleName());
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

	private Map<String, String> mapBulkUploadFields(String content, String delimiter, Map<String, String> configMap) {
		
		log.info("Entering mapBulkUploadFields of {}", this.getClass().getSimpleName());
		
		Map<String, String> contentMap = new HashMap<>();
		String[] invoiceDetails = content.split(delimiter);
		for (Map.Entry<String, String> entry : configMap.entrySet()) {
			if (!entry.getKey().equals(FileManagementConstant.FILE_CONFIG_DELIMITER)
					&& StringUtils.isNotBlank(entry.getValue())) {
				if (FileManagementConstant.ADDITIONAL_DB_FIELDS.contains(entry.getKey()) && entry.getValue() != null) {
					String[] additionalFields = entry.getValue().split(FileManagementConstant.PIPE_DELIMITER);
					contentMap.put(entry.getKey(), invoiceDetails[Integer.parseInt(additionalFields[1]) - 1]);
				} else {
					contentMap.put(entry.getKey(), invoiceDetails[Integer.parseInt(entry.getValue()) - 1]);
				}
			}
		}
		
		log.info("Exiting mapBulkUploadFields of {}", this.getClass().getSimpleName());
		return contentMap;
	}

}
