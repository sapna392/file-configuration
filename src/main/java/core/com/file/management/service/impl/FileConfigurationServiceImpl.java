package core.com.file.management.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import core.com.file.management.common.FileManagementConstant;
import core.com.file.management.entity.FileConfigurationEntity;
import core.com.file.management.exception.FileConfigurationException;
import core.com.file.management.model.AdditionalConfigField;
import core.com.file.management.model.FileConfigurationFields;
import core.com.file.management.model.FileConfigurationRest;
import core.com.file.management.repo.FileConfigurationRepo;
import core.com.file.management.service.FileConfigurationService;
import core.com.file.management.util.FileConfigurationUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileConfigurationServiceImpl extends AbstractConfigurationService implements FileConfigurationService {

	@Autowired
	FileConfigurationRepo fileConfigurationRepo;

	@Autowired
	FileConfigurationUtil fileManagementUtil;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	private Mapper mapper;

	@Transactional
	@Override
	public FileConfigurationRest saveFileConfiguration(FileConfigurationRest configurationRest)
			throws FileConfigurationException {

		log.info("Entering saveFileConfiguration of {}", this.getClass().getSimpleName());

		long count = fileConfigurationRepo.checkIfConfigurationExists(configurationRest.getImCode());
		FileConfigurationEntity fileConfigurationEntity = null;
		FileConfigurationFields configurationFields = configurationRest.getConfigurationFields();
		if (count == 0) {
			fileConfigurationEntity = mapper.map(configurationRest, FileConfigurationEntity.class);
			mapper.map(configurationFields, fileConfigurationEntity);
			if (CollectionUtils.isNotEmpty(configurationFields.getAdditionalFieldList())) {
				populateEntityAdditionalFields(configurationFields.getAdditionalFieldList(), fileConfigurationEntity);
			}
			fileConfigurationEntity.setCreatedBy(configurationRest.getImCode());
		} else {
			List<FileConfigurationEntity> existingConfigurationEntityList = fileConfigurationRepo
					.getFileConfigurationWithoutAdditionalFields(configurationRest.getImCode());
			fileConfigurationEntity = mapper.map(existingConfigurationEntityList.get(0),FileConfigurationEntity.class);
			fileConfigurationEntity.setImCode(configurationRest.getImCode());
			fileConfigurationEntity.setFileStructure(configurationRest.getFileStructure());
			if (FileManagementConstant.DELIMITER.equals(configurationRest.getFileStructure())) {
				fileConfigurationEntity.setFileDelimiter(configurationRest.getFileDelimiter());
			}
			fileConfigurationEntity.setInvoiceNumber(configurationFields.getInvoiceNumber());
			fileConfigurationEntity.setInvoiceAmount(configurationFields.getInvoiceAmount());
			fileConfigurationEntity.setInvoiceDate(configurationFields.getInvoiceDate());
			fileConfigurationEntity.setVendorCode(configurationFields.getVendorCode());
			fileConfigurationEntity.setVendorName(configurationFields.getVendorName());
			fileConfigurationEntity.setPaymentIdentifier(configurationFields.getPaymentIdentifier());
			fileConfigurationEntity.setDueDate(configurationFields.getDueDate());
			fileConfigurationEntity.setProcessingDate(configurationFields.getProcessingDate());
			populateEntityAdditionalFields(configurationFields.getAdditionalFieldList(), fileConfigurationEntity);
			
		}
		fileConfigurationEntity.setUpdatedBy(configurationRest.getImCode());

		fileConfigurationRepo.save(fileConfigurationEntity);

		List<FileConfigurationEntity> savedFileConfigurationEntityList = fileConfigurationRepo
				.findByImCode(configurationRest.getImCode());
		FileConfigurationRest savedConfigurationRest = mapToFileConfigurationRest(
				savedFileConfigurationEntityList.get(0));

		log.info("Exiting saveFileConfiguration of {}", this.getClass().getSimpleName());
		return savedConfigurationRest;
	}

	@Override
	public FileConfigurationRest viewFileConfiguration(String imCode) {

		log.info("Entering viewFileConfiguration of {}", this.getClass().getSimpleName());

		FileConfigurationRest configurationRest = null;
		List<FileConfigurationEntity> fileConfigurationEntityList = fileConfigurationRepo.findByImCode(imCode);
		if (CollectionUtils.isNotEmpty(fileConfigurationEntityList)) {
			configurationRest = mapToFileConfigurationRest(fileConfigurationEntityList.get(0));
		}

		log.info("Exiting viewFileConfiguration of {}", this.getClass().getSimpleName());
		return configurationRest;
	}

	private FileConfigurationRest mapToFileConfigurationRest(FileConfigurationEntity configurationEntity) {

		log.info("Entering mapToFileConfigurationRest of {}", this.getClass().getSimpleName());
		
		FileConfigurationRest savedConfigurationRest = mapper.map(configurationEntity, FileConfigurationRest.class);
		FileConfigurationFields fileConfigurationFields = mapper.map(configurationEntity,
				FileConfigurationFields.class);
		savedConfigurationRest.setConfigurationFields(fileConfigurationFields);
		Map<String, String> configMap = objectMapper.convertValue(configurationEntity, Map.class);
		if (configMap.containsKey(FileManagementConstant.ADDITIONAL_FIELD_1)
				&& (configMap.get(FileManagementConstant.ADDITIONAL_FIELD_1) != null)) {
			List<AdditionalConfigField> addConfigFieldList = getAdditionalConfigList(configMap);
			savedConfigurationRest.getConfigurationFields().setAdditionalFieldList(addConfigFieldList);
		}
		
		log.info("Exiting mapToFileConfigurationRest of {}", this.getClass().getSimpleName());
		return savedConfigurationRest;
	}
	
}