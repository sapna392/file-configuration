package core.com.file.management.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang3.EnumUtils;
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
import core.com.file.management.entity.FileConfigurationEntity;
import core.com.file.management.entity.VendorBulkInvoiceUploadEntity;
import core.com.file.management.entity.VendorTxnInvoiceEntity;
import core.com.file.management.exception.VendorBulkUploadException;
import core.com.file.management.model.ResponseMetadata;
import core.com.file.management.model.VendorBulkInvoiceUploadResponse;
import core.com.file.management.model.VendorBulkInvoiceUploadRest;
import core.com.file.management.model.VendorInvoiceStatus;
import core.com.file.management.model.VendorTxnInvoiceRest;
import core.com.file.management.repo.FileConfigurationRepo;
import core.com.file.management.repo.VendorBulkInvoiceUploadRepo;
import core.com.file.management.repo.VendorTxnInvoiceRepo;
import core.com.file.management.service.VendorBulkInvoiceUploadService;
import core.com.file.management.util.FileConfigurationUtil;
import core.com.file.management.validator.VendorBulkUploadValidator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class VendorBulkInvoiceUploadServiceImpl implements VendorBulkInvoiceUploadService {

	@Value("${core.scfu.max.file.size}")
	private Integer MAX_FILE_SIZE;

	@Autowired
	private FileConfigurationUtil fileManagementUtil;

	@Autowired
	private VendorBulkUploadValidator vendorBulkUploadValidator;

	@Autowired
	private VendorBulkInvoiceUploadRepo vendorBulkInvoiceUploadRepo;

	@Autowired
	private FileConfigurationRepo fileConfigurationRepo;

	@Autowired
	private VendorTxnInvoiceRepo vendorTxnInvoiceRepo;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private Mapper mapper;

	@Override
	public List<VendorTxnInvoiceRest> upload(MultipartFile file, String imCode) throws VendorBulkUploadException {

		log.info("Entering upload of {}", this.getClass().getSimpleName());

		List<FileConfigurationEntity> fileConfigurationEntityList = fileConfigurationRepo.getFileConfiguration(imCode);
		if (CollectionUtils.isEmpty(fileConfigurationEntityList)) {
			throw new VendorBulkUploadException(ErrorCode.FILE_CONFIG_DOESNOT_EXISTS);
		}
		FileConfigurationEntity fileConfigurationEntity = fileConfigurationEntityList.get(0);
		vendorBulkUploadValidator.validateUploadedFile(file, fileConfigurationEntity.getFileStructure());

		List<VendorTxnInvoiceRest> vendorTxnInvoiceRestList = new ArrayList<>();
		List<String> contentList = null;
		int count = 0;
		double amount = 0.00;
		
		InputStream inputStream = null;
		byte[] inputStreamByte = null;
		try {
			inputStream = file.getInputStream();
			inputStreamByte = inputStream.readAllBytes();
		} catch (IOException exp) {
			throw new VendorBulkUploadException(ErrorCode.FILE_PROCESSING_ERROR);
		}
		if (FileManagementConstant.XLS_MIME_TYPE.equals(file.getContentType())) {
			contentList = fileManagementUtil.readFromExcelWorkbook(inputStream);
		} else if (FileManagementConstant.TXT_MIME_TYPE.equals(file.getContentType())
				|| FileManagementConstant.CSV_MIME_TYPE.equals(file.getContentType())) {

			contentList = Arrays.asList(
					new String(inputStreamByte).split(FileManagementConstant.LINE_DELIMITER));
		}

		contentList = contentList.stream().filter(cl -> StringUtils.isNotEmpty(cl)).collect(Collectors.toList());
		Map<String, String> configMap = objectMapper.convertValue(fileConfigurationEntity, Map.class);
		configMap = configMap.entrySet().stream().filter(cm -> {
			if (StringUtils.isBlank(cm.getValue()) || FileManagementConstant.FILE_CONFIG_DELIMITER.equals(cm.getKey())
					|| FileManagementConstant.FILE_CONFIG_STRUCTURE.equals(cm.getKey())
					|| FileManagementConstant.CREATED.equals(cm.getKey())
					|| FileManagementConstant.UPDATED.equals(cm.getKey())) {
				return false;
			} else {
				return true;
			}
		}).collect(Collectors.toMap(cm -> cm.getKey(), cm -> cm.getValue()));
		if (FileManagementConstant.TXT_MIME_TYPE.equals(file.getContentType())) {
			for (String content : contentList) {
				Map<String, String> contentMap = new HashMap<>();
				VendorTxnInvoiceRest vendorTxnInvoiceRest = new VendorTxnInvoiceRest();
				if (FileManagementConstant.FILE_CONFIG_DELIMITER.equals(fileConfigurationEntity.getFileStructure())) {
					if (!content.contains(fileConfigurationEntity.getFileDelimiter())) {
						throw new VendorBulkUploadException(ErrorCode.FILE_CONFIG_DOESNOT_MATCH);
					} else {
						contentMap = mapBulkUploadFields(content, fileConfigurationEntity.getFileDelimiter(),
								configMap);
					}
					vendorTxnInvoiceRest = mapToVendorTxnInvoiceRest(imCode, contentMap);
					count++;
					amount += vendorTxnInvoiceRest.getInvoiceAmount();
					vendorTxnInvoiceRestList.add(vendorTxnInvoiceRest);
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
						vendorTxnInvoiceRest = mapToVendorTxnInvoiceRest(imCode, contentMap);
						count++;
						amount += vendorTxnInvoiceRest.getInvoiceAmount();
						vendorTxnInvoiceRestList.add(vendorTxnInvoiceRest);
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
			if (headers.equalsIgnoreCase(contentList.get(0))) {
				throw new VendorBulkUploadException(ErrorCode.FILE_CONFIG_DOESNOT_MATCH);
			}
			contentList.remove(0);
			for (String content : contentList) {
				VendorTxnInvoiceRest vendorTxnInvoiceRest = new VendorTxnInvoiceRest();
				Map<String, String> contentMap = mapBulkUploadFields(content, FileManagementConstant.COMMA, configMap);
				vendorTxnInvoiceRest = mapToVendorTxnInvoiceRest(imCode, contentMap);
				count++;
				amount += vendorTxnInvoiceRest.getInvoiceAmount();
				vendorTxnInvoiceRestList.add(vendorTxnInvoiceRest);
			}
		}

		// Saving file to local starts
		String contentHash = null;
		try {
			contentHash = fileManagementUtil.getContentHash(file.getOriginalFilename());
			String filePath = fileManagementUtil.getFilePath(contentHash);
			File savedFile = new File(filePath);
			if (!savedFile.exists())
				savedFile.mkdirs();
			savedFile = new File(filePath, file.getOriginalFilename());
			savedFile.createNewFile();
			try(OutputStream outStream = new FileOutputStream(savedFile)){
				outStream.write(file.getInputStream().readAllBytes());
			}
		} catch (IOException | NoSuchAlgorithmException exp) {
			throw new VendorBulkUploadException(ErrorCode.FILE_PROCESSING_ERROR);
		}
		// Saving file to local ends
		/*
		 * if (FileManagementConstant.EXCEL_MIME_TYPE.equals(file.getContentType())) {
		 * workbook = createExcelWorkbook(decodedString);
		 * workbook.write(fileOutputStream); } else {
		 * fileOutputStream.write(decodedString.getBytes()); }
		 */
		VendorBulkInvoiceUploadEntity vendorBulkInvoiceUploadEntity = new VendorBulkInvoiceUploadEntity();
		vendorBulkInvoiceUploadEntity.setImCode(imCode);
		vendorBulkInvoiceUploadEntity.setName(file.getOriginalFilename());
		vendorBulkInvoiceUploadEntity.setStatus(VendorInvoiceStatus.PENDING);
		vendorBulkInvoiceUploadEntity.setType(file.getContentType());
		vendorBulkInvoiceUploadEntity.setInvoiceCount(count);
		vendorBulkInvoiceUploadEntity.setTotalAmount(amount);
		vendorBulkInvoiceUploadEntity.setHash(contentHash);
		String bulkFileGuid = fileManagementUtil.getGuid(FileManagementConstant.BULK_UPLOAD);
		vendorBulkInvoiceUploadEntity.setGuid(bulkFileGuid);
		vendorBulkInvoiceUploadEntity.setCreatedBy(imCode);
		vendorBulkInvoiceUploadEntity.setUpdatedBy(imCode);
		VendorBulkInvoiceUploadEntity savedEntity = vendorBulkInvoiceUploadRepo.save(vendorBulkInvoiceUploadEntity);

		List<VendorTxnInvoiceEntity> vendorTxnInvoiceEntityList = vendorTxnInvoiceRestList.stream().map(vtir -> {
			VendorTxnInvoiceEntity entity = mapper.map(vtir, VendorTxnInvoiceEntity.class);
			entity.setFileId(savedEntity.getId().toString());
			entity.setCreatedBy(imCode);
			entity.setUpdatedBy(imCode);
			return entity;
		}).collect(Collectors.toList());

		vendorTxnInvoiceRepo.saveAll(vendorTxnInvoiceEntityList);

		log.info("Exiting upload of {}", this.getClass().getSimpleName());
		return vendorTxnInvoiceRestList;
	}

	@Override
	public VendorBulkInvoiceUploadResponse getUploadFileDetails(Pageable pageable, String status, String imCode) {

		log.info("Entering getUploadFileDetails of {}", this.getClass().getSimpleName());

		VendorBulkInvoiceUploadResponse bulkInvoiceUploadResponse = new VendorBulkInvoiceUploadResponse();
		if(StringUtils.isNotBlank(status) && !EnumUtils.isValidEnum(VendorInvoiceStatus.class, status)) {
			bulkInvoiceUploadResponse.setStatus(FileManagementConstant.FAILURE);
			bulkInvoiceUploadResponse.setStatus_code(String.valueOf(HttpStatus.BAD_REQUEST.value()));
			bulkInvoiceUploadResponse.setStatus_msg(status);
			bulkInvoiceUploadResponse.setStatus_msg(String.format(FileManagementConstant.INVALID_STATUS, status));
		} else {
			List<VendorBulkInvoiceUploadRest> vendorBulkInvoiceUploadRestList = null;
			Page<VendorBulkInvoiceUploadEntity> bulkUploadPage = null;
			if (StringUtils.isBlank(status)) {
				bulkUploadPage = vendorBulkInvoiceUploadRepo.findByImCode(imCode, pageable);
			} else {
				bulkUploadPage = vendorBulkInvoiceUploadRepo.findByStatusAndImCode(status, imCode, pageable);
			}

			if (CollectionUtils.isNotEmpty(bulkUploadPage.getContent())) {
				vendorBulkInvoiceUploadRestList = bulkUploadPage.getContent().stream()
						.map(bup -> mapper.map(bup, VendorBulkInvoiceUploadRest.class)).collect(Collectors.toList());
				bulkInvoiceUploadResponse.setStatus_msg(FileManagementConstant.FILE_DTLS_FETCH_SUCCESS);
			} else {
				bulkInvoiceUploadResponse.setStatus_msg(FileManagementConstant.FILE_CONFIG_DOESNOT_EXISTS);
			}

			ResponseMetadata metadata = new ResponseMetadata();
			metadata.setElements(bulkUploadPage.getTotalElements());
			metadata.setTotalPages(bulkUploadPage.getTotalPages());
			metadata.setSize(bulkUploadPage.getSize());
			metadata.setPage(bulkUploadPage.getNumber());
			bulkInvoiceUploadResponse.setMetadata(metadata);
			bulkInvoiceUploadResponse.setData(vendorBulkInvoiceUploadRestList);
			bulkInvoiceUploadResponse.setStatus(FileManagementConstant.SUCCESS);
			bulkInvoiceUploadResponse.setStatus_code(String.valueOf(HttpStatus.OK.value()));
		}
		
		log.info("Exiting getUploadFileDetails of {}", this.getClass().getSimpleName());
		return bulkInvoiceUploadResponse;
	}

	@Override
	public MultipartFile getUploadFileById(Long id, String userId, String serType) throws VendorBulkUploadException {

		log.info("Entering getUploadFileById of {}", this.getClass().getSimpleName());

		VendorBulkInvoiceUploadEntity uploadFileEntity = vendorBulkInvoiceUploadRepo.getFileById(id, userId);
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

		/*
		 * InputStream inputStream = null; vendorBulkInvoiceUploadEntity
		 * uploadFileEntity = vendorBulkInvoiceUploadRepo.getById(Long.valueOf(id));
		 * BulkUploadFileRest uploadFileRest = mapper.map(uploadFileEntity,
		 * BulkUploadFileRest.class); if
		 * (FileManagementConstant.XLS_FILE.equals(uploadFileRest.getType())) { String
		 * filePath = fileManagementUtil.getFilePath(uploadFileEntity.getHash(),
		 * uploadFileRest.getName(), uploadFileRest.getType()); try { inputStream = new
		 * InputStream(filePath); String content = readFromExcelWorkbook(inputStream);
		 * uploadFileRest.setContent(content); } catch (FileNotFoundException e) { throw
		 * new FileConfigurationException(ErrorCode.FILE_PROCESSING_ERROR); } catch
		 * (IOException e) { throw new
		 * FileConfigurationException(ErrorCode.FILE_PROCESSING_ERROR); } finally { try
		 * { inputStream.close(); } catch (IOException e) { throw new
		 * FileConfigurationException(ErrorCode.FILE_PROCESSING_ERROR); } }
		 */

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

	/*
	 * private HSSFWorkbook createExcelWorkbook(String uploadDecodedContent) {
	 * HSSFWorkbook workbook = new HSSFWorkbook(); HSSFSheet sheet =
	 * workbook.createSheet(); List<String> contentList =
	 * Arrays.asList(uploadDecodedContent.split(FileManagementConstant.
	 * LINE_DELIMITER)); int rowNum = 0; contentList.forEach(cl -> { HSSFRow row =
	 * sheet.createRow(Math.abs(~rowNum)); // increment without addition since
	 * addition is blocked List<String> clList =
	 * Arrays.asList(cl.split(FileManagementConstant.COMMA)); int cellNum = 0;
	 * clList.forEach(cll -> { HSSFCell cell = row.createCell(Math.abs(~cellNum));
	 * cell.setCellValue((String) cll); }); }); return workbook; }
	 */

	private Map<String, String> mapBulkUploadFields(String content, String delimiter, Map<String, String> configMap) {

		log.info("Entering mapBulkUploadFields of {}", this.getClass().getSimpleName());

		Map<String, String> contentMap = new HashMap<>();
		String[] invoiceDetails = content.split(delimiter);
		for (Map.Entry<String, String> entry : configMap.entrySet()) {
			if (StringUtils.isNotBlank(entry.getValue())) {
				if (FileManagementConstant.ADDITIONAL_DB_FIELDS.contains(entry.getKey()) && entry.getValue() != null) {
					String[] additionalFields = entry.getValue().split(FileManagementConstant.PIPE_DELIMITER);
					contentMap.put(entry.getKey(), additionalFields[0].concat(FileManagementConstant.PIPE)
							.concat(invoiceDetails[Integer.parseInt(additionalFields[1]) - 1]));
				} else {
					contentMap.put(entry.getKey(), invoiceDetails[Integer.parseInt(entry.getValue()) - 1]);
				}
			}
		}

		log.info("Exiting mapBulkUploadFields of {}", this.getClass().getSimpleName());
		return contentMap;
	}
	
	private VendorTxnInvoiceRest mapToVendorTxnInvoiceRest(String imCode, Map<String, String> contentMap)
			throws VendorBulkUploadException {

		log.info("Entering mapToVendorTxnInvoiceRest of {}", this.getClass().getSimpleName());

		VendorTxnInvoiceRest vendorTxnInvoiceRest = new VendorTxnInvoiceRest();
		vendorTxnInvoiceRest = objectMapper.convertValue(contentMap, VendorTxnInvoiceRest.class);
		vendorTxnInvoiceRest.setImCode(imCode);
		vendorTxnInvoiceRest.setStatus(VendorInvoiceStatus.PENDING_AUHTORIZATION);
		vendorBulkUploadValidator.validateInvoiceDetails(vendorTxnInvoiceRest);

		log.info("Exiting mapToVendorTxnInvoiceRest of {}", this.getClass().getSimpleName());
		return vendorTxnInvoiceRest;
	}

}
