package core.com.file.management.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import core.com.file.management.common.ErrorCode;
import core.com.file.management.common.FileManagementConstant;
import core.com.file.management.entity.ErrorFileConfigurationEntity;
import core.com.file.management.entity.ErrorFileDetailsEntity;
import core.com.file.management.entity.VendorBulkInvoiceUploadEntity;
import core.com.file.management.entity.VendorTxnInvoiceEntity;
import core.com.file.management.exception.VendorBulkUploadException;
import core.com.file.management.model.ErrorInvoiceFileRest;
import core.com.file.management.model.ResponseMetadata;
import core.com.file.management.model.VendorBulkInvoiceUploadRest;
import core.com.file.management.model.VendorInvoiceStatus;
import core.com.file.management.model.VendorTxnInvoiceResponse;
import core.com.file.management.model.VendorTxnInvoiceRest;
import core.com.file.management.repo.ErrorFileConfigurationRepo;
import core.com.file.management.repo.ErrorFileDetailsRepo;
import core.com.file.management.repo.VendorBulkInvoiceUploadRepo;
import core.com.file.management.repo.VendorTxnInvoiceRepo;
import core.com.file.management.service.VendorTxnInvoiceService;
import core.com.file.management.util.FileManagementUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class VendorTxnInvoiceServiceImpl implements VendorTxnInvoiceService {

	@Autowired
	Mapper mapper;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private FileManagementUtil fileManagementUtil;

	@Autowired
	private ErrorFileDetailsRepo errorFileDetailsRepo;

	@Autowired
	private VendorTxnInvoiceRepo vendorTxnInvoiceRepo;

	@Autowired
	private ErrorFileConfigurationRepo errorFileConfigurationRepo;

	@Autowired
	private VendorBulkInvoiceUploadRepo vendorBulkInvoiceUploadRepo;

	@Override
	public VendorBulkInvoiceUploadRest sumbitVendorTxnDetails(VendorBulkInvoiceUploadRest vendorBulkInvoiceUploadRest,
			String imCode) throws VendorBulkUploadException {

		VendorBulkInvoiceUploadEntity vendorBulkInvoiceUploadEntity = mapper.map(vendorBulkInvoiceUploadRest,
				VendorBulkInvoiceUploadEntity.class);

		String contentHash;
		try {
			contentHash = fileManagementUtil.getContentHash(vendorBulkInvoiceUploadRest.getName());
		} catch (NoSuchAlgorithmException e) {
			throw new VendorBulkUploadException(ErrorCode.FILE_PROCESSING_ERROR);
		}

		vendorBulkInvoiceUploadEntity.setHash(contentHash);
		vendorBulkInvoiceUploadEntity.setCreatedBy(imCode);
		vendorBulkInvoiceUploadEntity.setUpdatedBy(imCode);
		VendorBulkInvoiceUploadEntity savedEntity = vendorBulkInvoiceUploadRepo.save(vendorBulkInvoiceUploadEntity);
		mapper.map(savedEntity, vendorBulkInvoiceUploadRest);

		List<VendorTxnInvoiceEntity> vendorTxnInvoiceEntityList = vendorBulkInvoiceUploadRest
				.getVendorTxnInvoiceRestList().stream().map(vtir -> {
					VendorTxnInvoiceEntity entity = mapper.map(vtir, VendorTxnInvoiceEntity.class);
					entity.setFileId(savedEntity.getId().toString());
					entity.setCreatedBy(imCode);
					entity.setUpdatedBy(imCode);
					return entity;
				}).toList();

		vendorTxnInvoiceEntityList = vendorTxnInvoiceRepo.saveAll(vendorTxnInvoiceEntityList);
		List<VendorTxnInvoiceRest> vendorTxnInvoiceRestList = vendorTxnInvoiceEntityList.stream()
				.map(vtie -> mapper.map(vtie, VendorTxnInvoiceRest.class)).toList();
		vendorBulkInvoiceUploadRest.setVendorTxnInvoiceRestList(vendorTxnInvoiceRestList);
		return vendorBulkInvoiceUploadRest;
	}

	@Override
	public VendorTxnInvoiceResponse getVendorTxnInvoiceDetails(Pageable pageable, String status, String imCode) {

		log.info("Entering getVendorTxnInvoiceDetails of {}", this.getClass().getSimpleName());

		VendorTxnInvoiceResponse vendorTxnInvoiceResponse = new VendorTxnInvoiceResponse();

		if (StringUtils.isNotBlank(status) && !EnumUtils.isValidEnum(VendorInvoiceStatus.class, status)) {
			vendorTxnInvoiceResponse.setStatus(FileManagementConstant.FAILURE);
			vendorTxnInvoiceResponse.setStatus_code(String.valueOf(HttpStatus.BAD_REQUEST.value()));
			vendorTxnInvoiceResponse.setStatus_msg(String.format(FileManagementConstant.INVALID_STATUS, status));
		} else {
			List<VendorTxnInvoiceRest> vendorTxnInvoiceRestList = null;
			Page<VendorTxnInvoiceEntity> vendorTxnInvoiceEntityPage = null;

			if (StringUtils.isBlank(status)) {
				vendorTxnInvoiceEntityPage = vendorTxnInvoiceRepo.findByImCode(imCode, pageable);
			} else {
				vendorTxnInvoiceEntityPage = vendorTxnInvoiceRepo.findByStatusAndImCode(imCode, status, pageable);
			}

			if (CollectionUtils.isNotEmpty(vendorTxnInvoiceEntityPage.getContent())) {
				vendorTxnInvoiceRestList = vendorTxnInvoiceEntityPage.getContent().stream()
						.map(vtir -> mapper.map(vtir, VendorTxnInvoiceRest.class)).toList();
				vendorTxnInvoiceResponse.setStatus_msg(FileManagementConstant.TXN_DTLS_FETCH_SUCCESS);
			} else {
				vendorTxnInvoiceResponse.setStatus_msg(FileManagementConstant.TXN_DTLS_DOESNOT_EXISTS);
			}

			ResponseMetadata metadata = new ResponseMetadata();
			metadata.setSize(vendorTxnInvoiceEntityPage.getSize());
			metadata.setPage(vendorTxnInvoiceEntityPage.getNumber());
			metadata.setTotalPages(vendorTxnInvoiceEntityPage.getTotalPages());
			metadata.setElements(vendorTxnInvoiceEntityPage.getTotalElements());
			vendorTxnInvoiceResponse.setMetadata(metadata);
			vendorTxnInvoiceResponse.getData().setVendorTxnInvoiceRestList(vendorTxnInvoiceRestList);
			vendorTxnInvoiceResponse.setStatus(FileManagementConstant.SUCCESS);
			vendorTxnInvoiceResponse.setStatus_code(String.valueOf(HttpStatus.OK.value()));
		}

		log.info("Exiting getVendorTxnInvoiceDetails of {}", this.getClass().getSimpleName());
		return vendorTxnInvoiceResponse;
	}

	@Override
	public List<VendorTxnInvoiceRest> authorizeTransaction(String imCode,
			List<VendorTxnInvoiceRest> vendorTxnInvoiceRestList) throws VendorBulkUploadException {

		log.info("Entering authorizeTransaction of {}", this.getClass().getSimpleName());

		ByteArrayInputStream byteArrayInputStream = null;
		List<VendorTxnInvoiceEntity> vendorTxnInvoiceEntityList = vendorTxnInvoiceRestList.stream()
				.map(vtir -> mapper.map(vtir, VendorTxnInvoiceEntity.class)).toList();
		vendorTxnInvoiceEntityList = vendorTxnInvoiceRepo.saveAll(vendorTxnInvoiceEntityList);

		List<VendorTxnInvoiceRest> savedVendorTxnInvoiceRestList = vendorTxnInvoiceEntityList.stream()
				.map(vtie -> mapper.map(vtie, VendorTxnInvoiceRest.class)).toList();

		// Call will be made to CBS to get the transaction status.
		// Hard coding alternate transaction status to failed to generate error file
		List<ErrorInvoiceFileRest> errorInvoiceFileRestList = savedVendorTxnInvoiceRestList.parallelStream()
				.map(svtir -> {
					ErrorInvoiceFileRest errorInvoiceFileRest = new ErrorInvoiceFileRest();
					mapper.map(svtir, errorInvoiceFileRest);
					errorInvoiceFileRest.setReferenceNo(fileManagementUtil.getGuid(FileManagementConstant.ERROR_FILE));
					errorInvoiceFileRest.setCreationTime(new Date());
					errorInvoiceFileRest.setReversalDate(new Date());
					return errorInvoiceFileRest;
				}).toList();

		long count = errorFileConfigurationRepo.checkIfConfigurationExists(imCode);
		if (count == 0) {
			throw new VendorBulkUploadException(ErrorCode.ERROR_FILE_CONFIG_DOESNOT_EXISTS);
		}
		List<ErrorFileConfigurationEntity> errorFileConfigurationEntityList = errorFileConfigurationRepo
				.getErrorFileConfiguration(imCode);
		Map<String, String> errorConfigMap = objectMapper.convertValue(errorFileConfigurationEntityList.get(0),
				Map.class);
		List<List<String>> reversalContentList = new ArrayList<>();
		List<List<String>> errorContentList = new ArrayList<>();
		String[] headerArr = new String[errorConfigMap.size()];
		errorConfigMap.entrySet().stream().forEach(cm -> {
			if (FileManagementConstant.ADDITIONAL_DB_FIELDS.contains(cm.getKey())) {
				String[] arr = cm.getValue().split(FileManagementConstant.PIPE_DELIMITER);
				headerArr[Integer.parseInt(arr[1]) - 1] = arr[0];
			} else {
				headerArr[Integer.parseInt(cm.getValue()) - 1] = cm.getKey();
			}
		});
		reversalContentList.add(Arrays.asList(headerArr));
		errorContentList.add(Arrays.asList(headerArr));

		errorInvoiceFileRestList.forEach(efr -> {
			List<String> content = new ArrayList<>();
			Map<String, String> invoiceFileMap = objectMapper.convertValue(efr, Map.class);
			invoiceFileMap.entrySet().stream().map(ifm -> {
				if (FileManagementConstant.ADDITIONAL_DB_FIELDS.contains(ifm.getKey())) {
					String[] arr = ifm.getValue().split(FileManagementConstant.PIPE_DELIMITER);
					return Map.entry(arr[0], arr[1]);
				} else {
					return null;
				}
			});
			if (VendorInvoiceStatus.FAILED.equals(efr.getStatus())) {
				errorContentList.get(0).forEach(cl -> content.add(invoiceFileMap.get(cl)));
				errorContentList.add(content);
			} else {
				reversalContentList.get(0).forEach(cl -> content.add(invoiceFileMap.get(cl)));
				reversalContentList.add(content);
			}
		});

		VendorBulkInvoiceUploadEntity vendorBulkInvoiceUploadEntity = vendorBulkInvoiceUploadRepo
				.getFileById(Long.valueOf(vendorTxnInvoiceRestList.get(0).getFileId()), imCode);

		try {
			ErrorFileDetailsEntity reversalFileDetailsEntity = new ErrorFileDetailsEntity();
			reversalFileDetailsEntity.setImCode(imCode);
			reversalFileDetailsEntity.setName(
					FileManagementConstant.REVERSAL_FILE.concat("_").concat(vendorBulkInvoiceUploadEntity.getName()));
			reversalFileDetailsEntity.setStatus(VendorInvoiceStatus.PROCESSED);
			reversalFileDetailsEntity.setType(vendorBulkInvoiceUploadEntity.getType());
			String reversalGuid = fileManagementUtil.getGuid(FileManagementConstant.ERROR_FILE);
			reversalFileDetailsEntity.setGuid(reversalGuid);
			reversalFileDetailsEntity.setHash(fileManagementUtil.getContentHash(vendorBulkInvoiceUploadEntity.getName()));
			reversalFileDetailsEntity.setIsErrorFile(Boolean.FALSE);
			errorFileDetailsRepo.save(reversalFileDetailsEntity);
			byteArrayInputStream = getByteArrayInputStream(reversalContentList, vendorBulkInvoiceUploadEntity.getType(),
					errorConfigMap.get(FileManagementConstant.FILE_CONFIG_DELIMITER));
			writeFileToDirectory(reversalFileDetailsEntity.getHash(), reversalFileDetailsEntity.getName(), byteArrayInputStream);
			if (CollectionUtils.isNotEmpty(errorContentList)) {
				ErrorFileDetailsEntity errorFileDetailsEntity = new ErrorFileDetailsEntity();
				errorFileDetailsEntity.setImCode(imCode);
				errorFileDetailsEntity.setName(
						FileManagementConstant.REVERSAL_FILE.concat("_").concat(vendorBulkInvoiceUploadEntity.getName()));
				errorFileDetailsEntity.setStatus(VendorInvoiceStatus.PROCESSED);
				errorFileDetailsEntity.setType(vendorBulkInvoiceUploadEntity.getType());
				String errorGuid = fileManagementUtil.getGuid(FileManagementConstant.ERROR_FILE);
				errorFileDetailsEntity.setGuid(errorGuid);
				errorFileDetailsEntity.setHash(fileManagementUtil.getContentHash(vendorBulkInvoiceUploadEntity.getName()));
				errorFileDetailsEntity.setIsErrorFile(Boolean.TRUE);
				errorFileDetailsRepo.save(reversalFileDetailsEntity);
				byteArrayInputStream = getByteArrayInputStream(errorContentList, vendorBulkInvoiceUploadEntity.getType(),
						errorConfigMap.get(FileManagementConstant.FILE_CONFIG_DELIMITER));
				writeFileToDirectory(reversalFileDetailsEntity.getHash(), reversalFileDetailsEntity.getName(), byteArrayInputStream);
			}
		} catch (NoSuchAlgorithmException | VendorBulkUploadException | IOException e) {
			throw new VendorBulkUploadException(ErrorCode.ERROR_FILE_PROCESSING_ERROR);
		}
		
		
		log.info("Exiting authorizeTransaction of {}", this.getClass().getSimpleName());
		return savedVendorTxnInvoiceRestList;
	}
	
	private ByteArrayInputStream getByteArrayInputStream(List<List<String>> contentList, String type, String delimiter)
			throws VendorBulkUploadException {

		ByteArrayInputStream byteArrayInputStream = null;
		try {
			switch (type) {
			case FileManagementConstant.CSV_MIME_TYPE:
				byteArrayInputStream = fileManagementUtil.writeToErrorCsvFile(contentList);
				break;
			case FileManagementConstant.XLS_MIME_TYPE:
				byteArrayInputStream = fileManagementUtil.writeToErrorXlsFile(contentList);
				break;
			default:
				byteArrayInputStream = fileManagementUtil.writeToErrorTxtFile(contentList, delimiter);
				break;
			}
		} catch (IOException ioe) {
			throw new VendorBulkUploadException(ErrorCode.ERROR_FILE_PROCESSING_ERROR);
		}

		return byteArrayInputStream;
	}
	
	private void writeFileToDirectory(String hash, String name, ByteArrayInputStream byteArrayInputStream)
			throws FileNotFoundException, IOException {
		String filePath = fileManagementUtil.getFilePath(hash);
		File savedFile = new File(filePath);
		if (!savedFile.exists())
			savedFile.mkdirs();
		savedFile = new File(filePath, name);
		if (savedFile.createNewFile()) {
			try (OutputStream outStream = new FileOutputStream(savedFile)) {
				outStream.write(byteArrayInputStream.readAllBytes());
			}
		}
	}

}
