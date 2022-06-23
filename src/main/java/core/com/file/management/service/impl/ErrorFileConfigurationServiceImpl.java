package core.com.file.management.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import core.com.file.management.common.FileManagementConstant;
import core.com.file.management.entity.ErrorFileConfigurationEntity;
import core.com.file.management.exception.FileConfigurationException;
import core.com.file.management.model.AdditionalConfigField;
import core.com.file.management.model.ErrorFileConfigurationFields;
import core.com.file.management.model.ErrorFileConfigurationRest;
import core.com.file.management.repo.ErrorFileConfigurationRepo;
import core.com.file.management.service.ErrorFileConfigurationService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ErrorFileConfigurationServiceImpl extends AbstractConfigurationService
		implements ErrorFileConfigurationService {

	@Autowired
	ErrorFileConfigurationRepo errorFileConfigurationRepo;

	@Autowired
	Mapper mapper;

	@Override
	public ErrorFileConfigurationRest saveFileConfiguration(ErrorFileConfigurationRest configurationRest)
			throws FileConfigurationException {

		log.info("Entering saveFileConfiguration of {}", this.getClass().getSimpleName());

		long count = errorFileConfigurationRepo.checkIfConfigurationExists(configurationRest.getImCode());
		ErrorFileConfigurationEntity errorFileConfigurationEntity = null;
		ErrorFileConfigurationFields configurationFields = configurationRest.getConfigurationFields();
		if (count == 0) {
			errorFileConfigurationEntity = mapper.map(configurationRest, ErrorFileConfigurationEntity.class);
			mapper.map(configurationFields, errorFileConfigurationEntity);
			if (CollectionUtils.isNotEmpty(configurationFields.getAdditionalFieldList())) {
				populateEntityAdditionalFields(configurationFields.getAdditionalFieldList(),
						errorFileConfigurationEntity);
			}
			errorFileConfigurationEntity.setCreated(new Date());
			errorFileConfigurationEntity.setCreatedBy(configurationRest.getImCode());
		} else {
			List<ErrorFileConfigurationEntity> existingConfigurationEntityList = errorFileConfigurationRepo
					.getErrorFileConfigurationWithoutAdditionalFields(configurationRest.getImCode());
			errorFileConfigurationEntity = mapper.map(existingConfigurationEntityList.get(0),
					ErrorFileConfigurationEntity.class);
			errorFileConfigurationEntity.setImCode(configurationRest.getImCode());
			errorFileConfigurationEntity.setFileStructure(configurationRest.getFileStructure());
			if (FileManagementConstant.DELIMITER.equals(configurationRest.getFileStructure())) {
				errorFileConfigurationEntity.setFileDelimiter(configurationRest.getFileDelimiter());
			}
			errorFileConfigurationEntity.setFileId(configurationFields.getFileId());
			errorFileConfigurationEntity.setReferenceNo(configurationFields.getReferenceNo());
			errorFileConfigurationEntity.setCreationTime(configurationFields.getCreationTime());
			errorFileConfigurationEntity.setInvoiceNumber(configurationFields.getInvoiceNumber());
			errorFileConfigurationEntity.setInvoiceAmount(configurationFields.getInvoiceAmount());
			errorFileConfigurationEntity.setInvoiceDate(configurationFields.getInvoiceDate());
			errorFileConfigurationEntity.setReversalDate(configurationFields.getReversalDate());
			errorFileConfigurationEntity.setVendorCode(configurationFields.getVendorCode());
			errorFileConfigurationEntity.setVendorName(configurationFields.getVendorName());
			errorFileConfigurationEntity.setStatus(configurationFields.getStatus());
			errorFileConfigurationEntity.setStatusDescription(configurationFields.getStatusDescription());
			errorFileConfigurationEntity.setEchequeNo(configurationFields.getEchequeNo());
			errorFileConfigurationEntity.setUpdated(new Date());
			populateEntityAdditionalFields(configurationFields.getAdditionalFieldList(),
					errorFileConfigurationEntity);
		}
		errorFileConfigurationEntity.setUpdated(new Date());
		errorFileConfigurationEntity.setUpdatedBy(configurationRest.getImCode());

		errorFileConfigurationRepo.save(errorFileConfigurationEntity);

		List<ErrorFileConfigurationEntity> savedErrorFileConfigurationEntityList = errorFileConfigurationRepo
				.findByImCode(configurationRest.getImCode());
		ErrorFileConfigurationRest savedConfigurationRest = mapToFileConfigurationRest(
				savedErrorFileConfigurationEntityList.get(0));

		log.info("Exiting saveFileConfiguration of {}", this.getClass().getSimpleName());
		return savedConfigurationRest;
	}

	@Override
	public ErrorFileConfigurationRest viewFileConfiguration(String imCode) {
		
		log.info("Entering viewFileConfiguration of {}", this.getClass().getSimpleName());

		ErrorFileConfigurationRest configurationRest = null;
		List<ErrorFileConfigurationEntity> errorFileConfigurationEntityList = errorFileConfigurationRepo
				.findByImCode(imCode);
		if (CollectionUtils.isNotEmpty(errorFileConfigurationEntityList)) {
			configurationRest = mapToFileConfigurationRest(errorFileConfigurationEntityList.get(0));
		}

		log.info("Exiting viewFileConfiguration of {}", this.getClass().getSimpleName());
		return configurationRest;
	}

	private ErrorFileConfigurationRest mapToFileConfigurationRest(
			ErrorFileConfigurationEntity configurationEntity) {
		
		log.info("Entering mapToFileConfigurationRest of {}", this.getClass().getSimpleName());

		ErrorFileConfigurationRest savedErrorConfigurationRest = mapper.map(configurationEntity,
				ErrorFileConfigurationRest.class);
		ErrorFileConfigurationFields errorFileConfigurationFields = mapper.map(configurationEntity,
				ErrorFileConfigurationFields.class);
		savedErrorConfigurationRest.setConfigurationFields(errorFileConfigurationFields);
		Map<String, String> configMap = objectMapper.convertValue(configurationEntity, Map.class);
		if (configMap.containsKey(FileManagementConstant.ADDITIONAL_FIELD_1)
				&& (configMap.get(FileManagementConstant.ADDITIONAL_FIELD_1) != null)) {
			List<AdditionalConfigField> addConfigFieldList = getAdditionalConfigList(configMap);
			savedErrorConfigurationRest.getConfigurationFields().setAdditionalFieldList(addConfigFieldList);
		}
		
		log.info("Exiting mapToFileConfigurationRest of {}", this.getClass().getSimpleName());
		return savedErrorConfigurationRest;
	}

}
