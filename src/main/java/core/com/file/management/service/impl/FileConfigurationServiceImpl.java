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
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import core.com.file.management.common.ErrorCode;
import core.com.file.management.common.FileManagementConstant;
import core.com.file.management.entity.FileConfigurationEntity;
import core.com.file.management.exception.FileConfigurationException;
import core.com.file.management.exception.NotFoundException;
import core.com.file.management.model.AdditionalConfigField;
import core.com.file.management.model.FileConfigurationFields;
import core.com.file.management.model.FileConfigurationRest;
import core.com.file.management.repo.FileConfigurationRepo;
import core.com.file.management.service.FileConfigurationService;
import core.com.file.management.util.FileConfigurationUtil;

@Service
public class FileConfigurationServiceImpl extends AbstractConfigurationService implements FileConfigurationService {

	public static final Logger LOGGER = LoggerFactory.getLogger(FileConfigurationServiceImpl.class);

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

		LOGGER.info("Entering saveFileConfiguration of " + FileConfigurationServiceImpl.class.getName());
		// have to validate user for the userId and userType

		long count = fileConfigurationRepo.checkIfConfigurationExists(configurationRest.getImCode());
		FileConfigurationEntity fileConfigurationEntity = null;
		Map<String, String> fileConfigEntityMap = null;
		FileConfigurationFields configurationFields = configurationRest.getConfigurationFields();
		if (count == 0) {
			fileConfigurationEntity = mapper.map(configurationRest, FileConfigurationEntity.class);
			mapper.map(configurationFields, fileConfigurationEntity);
			if (CollectionUtils.isNotEmpty(configurationFields.getAdditionalFieldList())) {
				
				fileConfigEntityMap =  populateEntityAdditionalFields(configurationFields.getAdditionalFieldList(),
						fileConfigurationEntity);
				fileConfigurationEntity = objectMapper.convertValue(fileConfigEntityMap, FileConfigurationEntity.class);
			}
			fileConfigurationEntity.setCreated(new Date());
			fileConfigurationEntity.setCreatedBy(configurationRest.getImCode());
		} else {
			FileConfigurationEntity existingConfigurationEntity = fileConfigurationRepo
					.getFileConfiguration(configurationRest.getImCode()).get(0);
			fileConfigurationEntity = mapper.map(existingConfigurationEntity, FileConfigurationEntity.class);
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
			fileConfigurationEntity.setUpdated(new Date());
			fileConfigEntityMap =  populateEntityAdditionalFields(configurationFields.getAdditionalFieldList(),
					fileConfigurationEntity);
			fileConfigurationEntity = objectMapper.convertValue(fileConfigEntityMap, FileConfigurationEntity.class);
		}
		fileConfigurationEntity.setUpdated(new Date());
		fileConfigurationEntity.setUpdatedBy(configurationRest.getImCode());

		fileConfigurationRepo.save(fileConfigurationEntity);

		FileConfigurationEntity savedFileConfigurationEntity = fileConfigurationRepo
				.getFileConfiguration(configurationRest.getImCode()).get(0);
		FileConfigurationRest savedConfigurationRest = mapToFileConfigurationRest(savedFileConfigurationEntity);

		LOGGER.info("Exiting saveFileConfiguration of " + FileConfigurationServiceImpl.class.getName());
		return savedConfigurationRest;
	}

	@Override
	public FileConfigurationRest viewFileConfiguration(String imCode) throws NotFoundException {

		LOGGER.info("Entering viewFileConfiguration of " + FileConfigurationServiceImpl.class.getName());
		// have to validate user for the userId and userType

		FileConfigurationRest configurationRest = null;
		List<FileConfigurationEntity> fileConfigurationEntityList = fileConfigurationRepo.getFileConfiguration(imCode);
		if (CollectionUtils.isNotEmpty(fileConfigurationEntityList)) {
			configurationRest = mapToFileConfigurationRest(fileConfigurationEntityList.get(0));
		}

		LOGGER.info("Exiting viewFileConfiguration of " + FileConfigurationServiceImpl.class.getName());
		return configurationRest;
	}

	private FileConfigurationRest mapToFileConfigurationRest(FileConfigurationEntity configurationEntity) {

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
		return savedConfigurationRest;
	}
	
}