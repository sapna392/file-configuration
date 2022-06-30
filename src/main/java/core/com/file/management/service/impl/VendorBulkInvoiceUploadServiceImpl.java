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
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
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
import core.com.file.management.exception.VendorBulkUploadException;
import core.com.file.management.model.ResponseMetadata;
import core.com.file.management.model.VendorBulkInvoiceUploadResponse;
import core.com.file.management.model.VendorBulkInvoiceUploadRest;
import core.com.file.management.model.VendorInvoiceStatus;
import core.com.file.management.model.VendorTxnInvoiceErrorRest;
import core.com.file.management.model.VendorTxnInvoiceRest;
import core.com.file.management.repo.FileConfigurationRepo;
import core.com.file.management.repo.VendorBulkInvoiceUploadRepo;
import core.com.file.management.service.VendorBulkInvoiceUploadService;
import core.com.file.management.util.FileConfigurationUtil;
import core.com.file.management.validator.VendorBulkUploadValidator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class VendorBulkInvoiceUploadServiceImpl implements VendorBulkInvoiceUploadService {

	@Autowired
	private Mapper mapper;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private FileConfigurationUtil fileConfigurationUtil;

	@Autowired
	private FileConfigurationRepo fileConfigurationRepo;
	
	@Autowired
	private VendorBulkUploadValidator vendorBulkUploadValidator;

	@Autowired
	private VendorBulkInvoiceUploadRepo vendorBulkInvoiceUploadRepo;

	@Override
	public VendorBulkInvoiceUploadRest upload(MultipartFile file, String imCode) throws VendorBulkUploadException {

		log.info("Entering upload of {}", this.getClass().getSimpleName());

		List<FileConfigurationEntity> fileConfigurationEntityList = fileConfigurationRepo.getFileConfiguration(imCode);
		if (CollectionUtils.isEmpty(fileConfigurationEntityList)) {
			throw new VendorBulkUploadException(ErrorCode.FILE_CONFIG_DOESNOT_EXISTS);
		}
		FileConfigurationEntity fileConfigurationEntity = fileConfigurationEntityList.get(0);
		vendorBulkUploadValidator.validateUploadedFile(file, fileConfigurationEntity.getFileStructure());

		List<VendorTxnInvoiceRest> vendorTxnInvoiceRestList = new ArrayList<>();
		List<VendorTxnInvoiceErrorRest> vendorTxnInvoiceErrorRestList = new ArrayList<>();
		VendorBulkInvoiceUploadRest vendorBulkInvoiceUploadRest = new VendorBulkInvoiceUploadRest();
		List<String> contentList = null;

		InputStream inputStream = null;
		byte[] inputStreamByte = null;
		try {
			inputStream = file.getInputStream();
			inputStreamByte = inputStream.readAllBytes();
		} catch (IOException exp) {
			throw new VendorBulkUploadException(ErrorCode.FILE_PROCESSING_ERROR);
		}
		if (FileManagementConstant.XLS_MIME_TYPE.equals(file.getContentType())) {
			contentList = fileConfigurationUtil.readFromExcelWorkbook(inputStream);
		} else if (FileManagementConstant.TXT_MIME_TYPE.equals(file.getContentType())
				|| FileManagementConstant.CSV_MIME_TYPE.equals(file.getContentType())) {
			contentList = Arrays.asList(new String(inputStreamByte).split(FileManagementConstant.LINE_DELIMITER));
		}
		contentList = contentList.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());

		Map<String, String> configMap = objectMapper.convertValue(fileConfigurationEntity, Map.class);
		configMap = configMap.entrySet().stream().filter(cm -> {
			boolean check = true;
			if (StringUtils.isBlank(cm.getValue()) || FileManagementConstant.FILE_CONFIG_DELIMITER.equals(cm.getKey())
					|| FileManagementConstant.FILE_CONFIG_STRUCTURE.equals(cm.getKey())
					|| FileManagementConstant.CREATED.equals(cm.getKey())
					|| FileManagementConstant.UPDATED.equals(cm.getKey())) {
				check = false;
			}
			return check;
		}).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		if (FileManagementConstant.TXT_MIME_TYPE.equals(file.getContentType())) {
			for (String content : contentList) {
				Map<String, String> contentMap = new HashMap<>();
				if (FileManagementConstant.FILE_CONFIG_DELIMITER.equals(fileConfigurationEntity.getFileStructure())) {
					if (!content.contains(fileConfigurationEntity.getFileDelimiter())) {
						throw new VendorBulkUploadException(ErrorCode.FILE_CONFIG_DOESNOT_MATCH);
					} else {
						contentMap = mapBulkUploadFields(content, fileConfigurationEntity.getFileDelimiter(),
								configMap);
					}
					mapToVendorTxnInvoiceRest(imCode, contentMap, vendorTxnInvoiceRestList,
							vendorTxnInvoiceErrorRestList);
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
						mapToVendorTxnInvoiceRest(imCode, contentMap, vendorTxnInvoiceRestList,
								vendorTxnInvoiceErrorRestList);
					}
				}
			}
		} else if (FileManagementConstant.XLS_MIME_TYPE.equals(file.getContentType())
				|| FileManagementConstant.CSV_MIME_TYPE.equals(file.getContentType())) {
			String[] headerArr = new String[configMap.size()];
			configMap.entrySet().stream().forEach(cm -> {
				if (FileManagementConstant.ADDITIONAL_DB_FIELDS.contains(cm.getKey())) {
					String[] arr = cm.getValue().split(FileManagementConstant.PIPE_DELIMITER);
					headerArr[Integer.parseInt(arr[1])-1] = arr[0];
				} else {
					headerArr[Integer.parseInt(cm.getValue())-1] = cm.getKey();
				}
			});
			
			List<String> headerList = Arrays.asList(headerArr); 
			List<String> contentHeaderList = Arrays.asList(contentList.get(0).split(FileManagementConstant.COMMA));
			boolean headerCheck = contentHeaderList.stream().anyMatch(cl -> {
				boolean check = false;
				if(headerList.indexOf(cl) != contentHeaderList.indexOf(cl)){
					check = true;
				}
				return check;
			});
			if (headerCheck) {
				throw new VendorBulkUploadException(ErrorCode.FILE_CONFIG_DOESNOT_MATCH);
			}
			contentList.remove(0);
			for (String content : contentList) {
				Map<String, String> contentMap = mapBulkUploadFields(content, FileManagementConstant.COMMA, configMap);
				mapToVendorTxnInvoiceRest(imCode, contentMap, vendorTxnInvoiceRestList, vendorTxnInvoiceErrorRestList);
			}
		}
		
		vendorBulkInvoiceUploadRest.setImCode(imCode);
		vendorBulkInvoiceUploadRest.setType(file.getContentType());
		vendorBulkInvoiceUploadRest.setName(file.getOriginalFilename());
		
		if(CollectionUtils.isEmpty(vendorTxnInvoiceErrorRestList)) {
			double totalInvoiceAmount = vendorTxnInvoiceRestList.stream()
					.mapToDouble(VendorTxnInvoiceRest::getInvoiceAmount).sum();
			vendorBulkInvoiceUploadRest.setTotalAmount(totalInvoiceAmount);
			vendorBulkInvoiceUploadRest.setStatus(VendorInvoiceStatus.PENDING);
			vendorBulkInvoiceUploadRest.setInvoiceCount(vendorTxnInvoiceRestList.size());
			vendorBulkInvoiceUploadRest.setVendorTxnInvoiceRestList(vendorTxnInvoiceRestList);
			String bulkFileGuid = fileConfigurationUtil.getGuid(FileManagementConstant.BULK_UPLOAD);
			vendorBulkInvoiceUploadRest.setGuid(bulkFileGuid);
			
			// Saving file to local starts
			String contentHash = null;
			try {
				contentHash = fileConfigurationUtil.getContentHash(vendorBulkInvoiceUploadRest.getName());
				String filePath = fileConfigurationUtil.getFilePath(contentHash);
				File savedFile = new File(filePath);
				if (!savedFile.exists())
					savedFile.mkdirs();
				savedFile = new File(filePath, vendorBulkInvoiceUploadRest.getName());
				if(savedFile.createNewFile()) {
					try (OutputStream outStream = new FileOutputStream(savedFile)) {
						outStream.write(file.getInputStream().readAllBytes());
					}
				}
			} catch (IOException | NoSuchAlgorithmException exp) {
				throw new VendorBulkUploadException(ErrorCode.FILE_PROCESSING_ERROR);
			}
			// Saving file to local ends
			
		} else {
			vendorBulkInvoiceUploadRest.setStatus(VendorInvoiceStatus.FAILED);
			vendorBulkInvoiceUploadRest.setInvoiceCount(vendorTxnInvoiceErrorRestList.size());
			vendorBulkInvoiceUploadRest.setVendorTxnInvoiceErrorRestList(vendorTxnInvoiceErrorRestList);
		}

		log.info("Exiting upload of {}", this.getClass().getSimpleName());
		return vendorBulkInvoiceUploadRest;
	}
	
	@Override
	public VendorBulkInvoiceUploadResponse getUploadFileDetails(Pageable pageable, String status, String imCode) {

		log.info("Entering getUploadFileDetails of {}", this.getClass().getSimpleName());

		VendorBulkInvoiceUploadResponse bulkInvoiceUploadResponse = new VendorBulkInvoiceUploadResponse();
		if (StringUtils.isNotBlank(status) && !EnumUtils.isValidEnum(VendorInvoiceStatus.class, status)) {
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
						.map(bup -> mapper.map(bup, VendorBulkInvoiceUploadRest.class)).toList();
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
		String filePath = fileConfigurationUtil.getFilePath(uploadFileEntity.getHash());
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

		log.info("Exiting getUploadFileById of {}", this.getClass().getSimpleName());
		return multipartFile;
	}

	@Override
	public InputStream download(String userId, String userType, String mediaType) throws VendorBulkUploadException {

		log.info("Entering download of {}", this.getClass().getSimpleName());

		List<FileConfigurationEntity> fileConfigurationEntityList = fileConfigurationRepo.getFileConfiguration(userId);
		ByteArrayInputStream byteArrayInputStream = null;
		if (CollectionUtils.isNotEmpty(fileConfigurationEntityList)) {
			Map<String, String> configMap = objectMapper.convertValue(fileConfigurationEntityList.get(0), Map.class);
			configMap = configMap.entrySet().stream().filter(cm -> {
				boolean check = true;
				if(StringUtils.isBlank(cm.getValue())
						&& FileManagementConstant.FILE_CONFIG_DELIMITER.equals(cm.getKey())) {
					check = false;
				}
				return check;
			}).sorted(Map.Entry.comparingByValue())
			.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
			
			List<String> sortedConfigMapKeys = configMap.entrySet().stream().map(cm -> {
				if (cm.getValue().contains(FileManagementConstant.PIPE)) {
					String[] strArr = cm.getValue().split(FileManagementConstant.PIPE_DELIMITER);
					return Map.entry(strArr[0], Integer.parseInt(strArr[1]));
				} else {
					return Map.entry(cm.getKey(), Integer.parseInt(cm.getValue()));
				}
			}).collect(Collectors.mapping(Entry::getKey, Collectors.toList()));
			
			try {
				switch(mediaType) {
					case FileManagementConstant.CSV_MIME_TYPE :
						byteArrayInputStream = fileConfigurationUtil.writeToCsvFile(sortedConfigMapKeys);
						break;
					case FileManagementConstant.XLS_MIME_TYPE :
						byteArrayInputStream = fileConfigurationUtil.writeToXlsFile(sortedConfigMapKeys);
						break;
					default :
						byteArrayInputStream = fileConfigurationUtil.writeToTxtFile(sortedConfigMapKeys,
								configMap.get(FileManagementConstant.FILE_CONFIG_DELIMITER));
						break;
				}
			} catch (IOException e) {
				throw new VendorBulkUploadException(e.getMessage());
			}
		} else {
			throw new VendorBulkUploadException(ErrorCode.FILE_CONFIG_DOESNOT_EXISTS);
		}
		
		log.info("Exiting download of {}", this.getClass().getSimpleName());
		return byteArrayInputStream;
	}

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

	private VendorTxnInvoiceRest mapToVendorTxnInvoiceRest(String imCode, Map<String, String> contentMap,
			List<VendorTxnInvoiceRest> vendorTxnInvoiceRestList,
			List<VendorTxnInvoiceErrorRest> vendorTxnInvoiceErrorRestList) {

		log.info("Entering mapToVendorTxnInvoiceRest of {}", this.getClass().getSimpleName());

		VendorTxnInvoiceRest vendorTxnInvoiceRest = objectMapper.convertValue(contentMap, VendorTxnInvoiceRest.class);
		vendorTxnInvoiceRest.setImCode(imCode);
		vendorTxnInvoiceRest.setStatus(VendorInvoiceStatus.PENDING_AUHTORIZATION);
		
		try {
			vendorBulkUploadValidator.validateInvoiceDetails(vendorTxnInvoiceRest);
			vendorTxnInvoiceRestList.add(vendorTxnInvoiceRest);
		} catch (VendorBulkUploadException e) {
			VendorTxnInvoiceErrorRest vendorTxnInvoiceErrorRest = new VendorTxnInvoiceErrorRest();
			vendorTxnInvoiceErrorRest.setInvoiceNumber(vendorTxnInvoiceRest.getInvoiceNumber());
			vendorTxnInvoiceErrorRest.setErrorDescription(e.getMessage());
			vendorTxnInvoiceErrorRestList.add(vendorTxnInvoiceErrorRest);
		}

		log.info("Exiting mapToVendorTxnInvoiceRest of {}", this.getClass().getSimpleName());
		return vendorTxnInvoiceRest;
	}

}
