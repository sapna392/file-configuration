package core.com.file.management.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import core.com.file.management.common.FileManagementConstant;
import core.com.file.management.entity.ReverseFileConfigurationEntity;
import core.com.file.management.exception.FileConfigurationException;
import core.com.file.management.exception.NotFoundException;
import core.com.file.management.model.AdditionalConfigField;
import core.com.file.management.model.ReverseFileConfigurationFields;
import core.com.file.management.model.ReverseFileConfigurationRest;
import core.com.file.management.repo.ReverseFileConfigurationRepo;
import core.com.file.management.service.ReverseFileConfigurationService;

@Service
public class ReverseFileConfigurationServiceImpl extends AbstractConfigurationService
		implements ReverseFileConfigurationService {

	public static final Logger LOGGER = LoggerFactory.getLogger(ReverseFileConfigurationServiceImpl.class);

	@Autowired
	ReverseFileConfigurationRepo reverseFileConfigurationRepo;

	@Autowired
	Mapper mapper;

	@Override
	public ReverseFileConfigurationRest saveFileConfiguration(ReverseFileConfigurationRest configurationRest)
			throws FileConfigurationException {

		LOGGER.info("Entering saveFileConfiguration of " + FileConfigurationServiceImpl.class.getName());
		// have to validate user for the userId and userType

		long count = reverseFileConfigurationRepo.checkIfConfigurationExists(configurationRest.getImCode());
		ReverseFileConfigurationEntity reverseFileConfigurationEntity = null;
		Map<String, String> reverseFileConfigEntityMap = null;
		ReverseFileConfigurationFields configurationFields = configurationRest.getConfigurationFields();
		if (count == 0) {
			reverseFileConfigurationEntity = mapper.map(configurationRest, ReverseFileConfigurationEntity.class);
			mapper.map(configurationFields, reverseFileConfigurationEntity);
			if (CollectionUtils.isNotEmpty(configurationFields.getAdditionalFieldList())) {
				reverseFileConfigEntityMap = populateEntityAdditionalFields(
						configurationFields.getAdditionalFieldList(), reverseFileConfigurationEntity);
				reverseFileConfigurationEntity = objectMapper.convertValue(reverseFileConfigEntityMap,
						ReverseFileConfigurationEntity.class);
			}
			reverseFileConfigurationEntity.setCreated(new Date());
			reverseFileConfigurationEntity.setCreatedBy(configurationRest.getImCode());
		} else {
			ReverseFileConfigurationEntity existingConfigurationEntity = reverseFileConfigurationRepo
					.getReverseFileConfiguration(configurationRest.getImCode()).get(0);
			reverseFileConfigurationEntity = mapper.map(existingConfigurationEntity,
					ReverseFileConfigurationEntity.class);
			reverseFileConfigurationEntity.setImCode(configurationRest.getImCode());
			reverseFileConfigurationEntity.setFileStructure(configurationRest.getFileStructure());
			if (FileManagementConstant.DELIMITER.equals(configurationRest.getFileStructure())) {
				reverseFileConfigurationEntity.setFileDelimiter(configurationRest.getFileDelimiter());
			}
			reverseFileConfigurationEntity.setFileId(configurationFields.getFileId());
			reverseFileConfigurationEntity.setReferenceNo(configurationFields.getReferenceNo());
			reverseFileConfigurationEntity.setCreationTime(configurationFields.getCreationTime());
			reverseFileConfigurationEntity.setInvoiceNumber(configurationFields.getInvoiceNumber());
			reverseFileConfigurationEntity.setInvoiceAmount(configurationFields.getInvoiceAmount());
			reverseFileConfigurationEntity.setInvoiceDate(configurationFields.getInvoiceDate());
			reverseFileConfigurationEntity.setReversalDate(configurationFields.getReversalDate());
			reverseFileConfigurationEntity.setVendorCode(configurationFields.getVendorCode());
			reverseFileConfigurationEntity.setVendorName(configurationFields.getVendorName());
			reverseFileConfigurationEntity.setStatus(configurationFields.getStatus());
			reverseFileConfigurationEntity.setStatusDescription(configurationFields.getStatusDescription());
			reverseFileConfigurationEntity.setEchequeNo(configurationFields.getEchequeNo());
			reverseFileConfigurationEntity.setUpdated(new Date());
			reverseFileConfigEntityMap = populateEntityAdditionalFields(configurationFields.getAdditionalFieldList(),
					reverseFileConfigurationEntity);
			reverseFileConfigurationEntity = objectMapper.convertValue(reverseFileConfigEntityMap,
					ReverseFileConfigurationEntity.class);
		}
		reverseFileConfigurationEntity.setUpdated(new Date());
		reverseFileConfigurationEntity.setUpdatedBy(configurationRest.getImCode());

		reverseFileConfigurationRepo.save(reverseFileConfigurationEntity);

		ReverseFileConfigurationEntity savedReverseFileConfigurationEntity = reverseFileConfigurationRepo
				.getReverseFileConfiguration(configurationRest.getImCode()).get(0);
		ReverseFileConfigurationRest savedConfigurationRest = mapToFileConfigurationRest(
				savedReverseFileConfigurationEntity);

		LOGGER.info("Exiting saveFileConfiguration of " + ReverseFileConfigurationServiceImpl.class.getName());
		return savedConfigurationRest;
	}

	@Override
	public ReverseFileConfigurationRest viewFileConfiguration(String imCode) throws NotFoundException {
		LOGGER.info("Entering viewFileConfiguration of " + ReverseFileConfigurationServiceImpl.class.getName());
		// have to validate user for the userId and userType

		ReverseFileConfigurationRest configurationRest = null;
		List<ReverseFileConfigurationEntity> reverseFileConfigurationEntityList = reverseFileConfigurationRepo
				.getReverseFileConfiguration(imCode);
		if (CollectionUtils.isNotEmpty(reverseFileConfigurationEntityList)) {
			configurationRest = mapToFileConfigurationRest(reverseFileConfigurationEntityList.get(0));
		}

		LOGGER.info("Exiting viewFileConfiguration of " + ReverseFileConfigurationServiceImpl.class.getName());
		return configurationRest;
	}

	private ReverseFileConfigurationRest mapToFileConfigurationRest(
			ReverseFileConfigurationEntity configurationEntity) {

		ReverseFileConfigurationRest savedReverseConfigurationRest = mapper.map(configurationEntity,
				ReverseFileConfigurationRest.class);
		ReverseFileConfigurationFields reverseFileConfigurationFields = mapper.map(configurationEntity,
				ReverseFileConfigurationFields.class);
		savedReverseConfigurationRest.setConfigurationFields(reverseFileConfigurationFields);
		Map<String, String> configMap = objectMapper.convertValue(configurationEntity, Map.class);
		if (configMap.containsKey(FileManagementConstant.ADDITIONAL_FIELD_1)
				&& (configMap.get(FileManagementConstant.ADDITIONAL_FIELD_1) != null)) {
			List<AdditionalConfigField> addConfigFieldList = getAdditionalConfigList(configMap);
			savedReverseConfigurationRest.getConfigurationFields().setAdditionalFieldList(addConfigFieldList);
		}
		return savedReverseConfigurationRest;
	}

}
