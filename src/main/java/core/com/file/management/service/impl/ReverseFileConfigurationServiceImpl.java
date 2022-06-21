package core.com.file.management.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.dozer.Mapper;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReverseFileConfigurationServiceImpl extends AbstractConfigurationService
		implements ReverseFileConfigurationService {

	@Autowired
	ReverseFileConfigurationRepo reverseFileConfigurationRepo;

	@Autowired
	Mapper mapper;

	@Override
	public ReverseFileConfigurationRest saveFileConfiguration(ReverseFileConfigurationRest configurationRest)
			throws FileConfigurationException {

		log.info("Entering saveFileConfiguration of " + this.getClass().getSimpleName());

		long count = reverseFileConfigurationRepo.checkIfConfigurationExists(configurationRest.getImCode());
		ReverseFileConfigurationEntity reverseFileConfigurationEntity = null;
		ReverseFileConfigurationFields configurationFields = configurationRest.getConfigurationFields();
		if (count == 0) {
			reverseFileConfigurationEntity = mapper.map(configurationRest, ReverseFileConfigurationEntity.class);
			mapper.map(configurationFields, reverseFileConfigurationEntity);
			if (CollectionUtils.isNotEmpty(configurationFields.getAdditionalFieldList())) {
				populateEntityAdditionalFields(configurationFields.getAdditionalFieldList(),
						reverseFileConfigurationEntity);
			}
			reverseFileConfigurationEntity.setCreated(new Date());
			reverseFileConfigurationEntity.setCreatedBy(configurationRest.getImCode());
		} else {
			List<ReverseFileConfigurationEntity> existingConfigurationEntityList = reverseFileConfigurationRepo
					.getReverseFileConfigurationWithoutAdditionalFields(configurationRest.getImCode());
			reverseFileConfigurationEntity = mapper.map(existingConfigurationEntityList.get(0),
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
			populateEntityAdditionalFields(configurationFields.getAdditionalFieldList(),
					reverseFileConfigurationEntity);
		}
		reverseFileConfigurationEntity.setUpdated(new Date());
		reverseFileConfigurationEntity.setUpdatedBy(configurationRest.getImCode());

		reverseFileConfigurationRepo.save(reverseFileConfigurationEntity);

		List<ReverseFileConfigurationEntity> savedReverseFileConfigurationEntityList = reverseFileConfigurationRepo
				.findByImCode(configurationRest.getImCode());
		ReverseFileConfigurationRest savedConfigurationRest = mapToFileConfigurationRest(
				savedReverseFileConfigurationEntityList.get(0));

		log.info("Exiting saveFileConfiguration of " + this.getClass().getSimpleName());
		return savedConfigurationRest;
	}

	@Override
	public ReverseFileConfigurationRest viewFileConfiguration(String imCode) throws NotFoundException {
		
		log.info("Entering viewFileConfiguration of " + this.getClass().getSimpleName());

		ReverseFileConfigurationRest configurationRest = null;
		List<ReverseFileConfigurationEntity> reverseFileConfigurationEntityList = reverseFileConfigurationRepo
				.findByImCode(imCode);
		if (CollectionUtils.isNotEmpty(reverseFileConfigurationEntityList)) {
			configurationRest = mapToFileConfigurationRest(reverseFileConfigurationEntityList.get(0));
		}

		log.info("Exiting viewFileConfiguration of " + this.getClass().getSimpleName());
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
