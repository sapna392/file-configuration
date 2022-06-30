package core.com.file.management.service.impl;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import core.com.file.management.common.ErrorCode;
import core.com.file.management.common.FileManagementConstant;
import core.com.file.management.entity.VendorBulkInvoiceUploadEntity;
import core.com.file.management.entity.VendorTxnInvoiceEntity;
import core.com.file.management.exception.VendorBulkUploadException;
import core.com.file.management.model.ResponseMetadata;
import core.com.file.management.model.VendorBulkInvoiceUploadRest;
import core.com.file.management.model.VendorInvoiceStatus;
import core.com.file.management.model.VendorTxnInvoiceResponse;
import core.com.file.management.model.VendorTxnInvoiceRest;
import core.com.file.management.repo.VendorBulkInvoiceUploadRepo;
import core.com.file.management.repo.VendorTxnInvoiceRepo;
import core.com.file.management.service.VendorTxnInvoiceService;
import core.com.file.management.util.FileConfigurationUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class VendorTxnInvoiceServiceImpl implements VendorTxnInvoiceService {
	
	@Autowired
	private VendorBulkInvoiceUploadRepo vendorBulkInvoiceUploadRepo;
	
	@Autowired
	private VendorTxnInvoiceRepo vendorTxnInvoiceRepo;
	
	@Autowired
	private FileConfigurationUtil fileConfigurationUtil;
	
	@Autowired
	Mapper mapper;
	
	@Override
	public VendorBulkInvoiceUploadRest sumbitVendorTxnDetails(VendorBulkInvoiceUploadRest vendorBulkInvoiceUploadRest,
			String imCode) throws VendorBulkUploadException {

		VendorBulkInvoiceUploadEntity vendorBulkInvoiceUploadEntity = mapper.map(vendorBulkInvoiceUploadRest,
				VendorBulkInvoiceUploadEntity.class);

		String contentHash;
		try {
			contentHash = fileConfigurationUtil.getContentHash(vendorBulkInvoiceUploadRest.getName());
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
		
		if(StringUtils.isNotBlank(status) && !EnumUtils.isValidEnum(VendorInvoiceStatus.class, status)) {
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
			metadata.setElements(vendorTxnInvoiceEntityPage.getTotalElements());
			metadata.setTotalPages(vendorTxnInvoiceEntityPage.getTotalPages());
			metadata.setSize(vendorTxnInvoiceEntityPage.getSize());
			metadata.setPage(vendorTxnInvoiceEntityPage.getNumber());
			vendorTxnInvoiceResponse.setMetadata(metadata);
			vendorTxnInvoiceResponse.getData().setVendorTxnInvoiceRestList(vendorTxnInvoiceRestList);
			vendorTxnInvoiceResponse.setStatus(FileManagementConstant.SUCCESS);
			vendorTxnInvoiceResponse.setStatus_code(String.valueOf(HttpStatus.OK.value()));
		}
		
		log.info("Exiting getVendorTxnInvoiceDetails of {}", this.getClass().getSimpleName());
		return vendorTxnInvoiceResponse;
	}
	
	@Override
	public List<VendorTxnInvoiceRest> authorizeTransaction(List<VendorTxnInvoiceRest> vendorTxnInvoiceRestList) {
		
		log.info("Entering authorizeTransaction of {}", this.getClass().getSimpleName());
		
		List<VendorTxnInvoiceEntity> vendorTxnInvoiceEntityList = vendorTxnInvoiceRestList.stream()
				.map(vtir -> mapper.map(vtir, VendorTxnInvoiceEntity.class)).toList();
		vendorTxnInvoiceEntityList = vendorTxnInvoiceRepo.saveAll(vendorTxnInvoiceEntityList);

		List<VendorTxnInvoiceRest> savedVendorTxnInvoiceRestList = vendorTxnInvoiceEntityList.stream()
				.map(vtie -> mapper.map(vtie, VendorTxnInvoiceRest.class)).toList();
		
		log.info("Exiting authorizeTransaction of {}", this.getClass().getSimpleName());
		return savedVendorTxnInvoiceRestList;
	}

}
