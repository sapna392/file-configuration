package core.com.file.management.service.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import core.com.file.management.model.AdditionalConfigField;
import core.com.file.management.model.FileConfigurationFields;
import core.com.file.management.model.FileConfigurationRest;
import core.com.file.management.repo.FileConfigurationRepo;
import core.com.file.management.service.FileConfigurationService;
import core.com.file.management.util.FileManagementUtil;

@Service
public class FileConfigurationServiceImpl implements FileConfigurationService {

	public static final Logger LOGGER = LoggerFactory.getLogger(FileConfigurationServiceImpl.class);

	@Autowired
	FileConfigurationRepo fileConfigurationRepo;

	@Autowired
	FileManagementUtil fileManagementUtil;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	private Mapper mapper;

	@Transactional
	@Override
	public FileConfigurationRest saveFileConfiguration(FileConfigurationRest configurationRest)
			throws FileConfigurationException {

		LOGGER.info("Entering saveFileConfiguration of " + FileConfigurationServiceImpl.class.getName());

		long count = fileConfigurationRepo.checkIfConfigurationExists(configurationRest.getUserId(),
				configurationRest.getUserType());
		FileConfigurationEntity fileConfigurationEntity = null;
		FileConfigurationFields configurationFields = configurationRest.getConfigurationFields();
		if (count == 0) {
			fileConfigurationEntity = mapper.map(configurationRest, FileConfigurationEntity.class);
			mapper.map(configurationFields, fileConfigurationEntity);
			if (CollectionUtils.isNotEmpty(configurationFields.getAdditionalFieldList())) {
				fileConfigurationEntity = populateEntityAdditionalFields(configurationFields.getAdditionalFieldList(),
						fileConfigurationEntity);
			}
			fileConfigurationEntity.setCreated(new Date());
			fileConfigurationEntity.setUpdated(new Date());
		} else {
			FileConfigurationEntity existingConfigurationEntity = fileConfigurationRepo
					.getFileConfiguration(configurationRest.getUserId(), configurationRest.getUserType());
			fileConfigurationEntity = mapper.map(existingConfigurationEntity, FileConfigurationEntity.class);
			fileConfigurationEntity.setFileStructure(configurationRest.getFileStructure());
			if (FileManagementConstant.FILE_CONFIG_DELIMITER.equals(configurationRest.getFileStructure())) {
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
			fileConfigurationEntity = populateEntityAdditionalFields(configurationFields.getAdditionalFieldList(),
					fileConfigurationEntity);
		}

		fileConfigurationRepo.save(fileConfigurationEntity);

		FileConfigurationEntity savedFileConfigurationEntity = fileConfigurationRepo
				.getFileConfiguration(configurationRest.getUserId(), configurationRest.getUserType());
		FileConfigurationRest savedConfigurationRest = mapToFileConfigurationRest(savedFileConfigurationEntity);

		LOGGER.info("Exiting saveFileConfiguration of " + FileConfigurationServiceImpl.class.getName());
		return savedConfigurationRest;
	}

	@Override
	public FileConfigurationRest viewFileConfiguration(String userId, String userType)
			throws FileConfigurationException {

		LOGGER.info("Entering viewFileConfiguration of " + FileConfigurationServiceImpl.class.getName());
		// have to validate user for the userId and userType

		FileConfigurationRest configurationRest = null;
		FileConfigurationEntity fileConfigurationEntity = fileConfigurationRepo.getFileConfiguration(userId,
				userType);
		if (fileConfigurationEntity != null) {
			configurationRest = mapToFileConfigurationRest(fileConfigurationEntity);
		} else {
			throw new FileConfigurationException(ErrorCode.FILE_CONFIG_DOESNOT_EXISTS);
		}

		LOGGER.info("Exiting viewFileConfiguration of " + FileConfigurationServiceImpl.class.getName());
		return configurationRest;
	}

	private FileConfigurationEntity populateEntityAdditionalFields(
			List<AdditionalConfigField> additionalConfigFieldList, FileConfigurationEntity fileConfigurationEntity) {
		Map<String, String> fileConfigEntityMap = objectMapper.convertValue(fileConfigurationEntity, Map.class);
		int additionalFieldCount = 1;
		while(additionalFieldCount <=10) {
			fileConfigEntityMap.put(FileManagementConstant.ADDITIONAL_FIELD + additionalFieldCount++, null);
		}
		if(CollectionUtils.isNotEmpty(additionalConfigFieldList)) {
			additionalFieldCount = 1;
			for (AdditionalConfigField afl : additionalConfigFieldList) {
				StringBuilder sb = new StringBuilder();
				sb.append(afl.getConfigName());
				sb.append(FileManagementConstant.PIPE);
				sb.append(afl.getConfigPos());
				fileConfigEntityMap.put(FileManagementConstant.ADDITIONAL_FIELD + additionalFieldCount++, sb.toString());
			}
		}
		return objectMapper.convertValue(fileConfigEntityMap, FileConfigurationEntity.class);
	}

	private List<AdditionalConfigField> getAdditionalConfigList(Map<String, String> confifMap) {
		List<AdditionalConfigField> additionalConfigFieldList = new ArrayList<>();
		int additionalFieldCount = 1;
		for (Map.Entry<String, String> entry : confifMap.entrySet()) {
			if (entry.getKey().contains(FileManagementConstant.ADDITIONAL_FIELD + additionalFieldCount) && StringUtils
					.isNotBlank(confifMap.get(FileManagementConstant.ADDITIONAL_FIELD + additionalFieldCount))) {
				AdditionalConfigField additionalConfigField = new AdditionalConfigField();
				String[] additionalFields = confifMap
						.get(FileManagementConstant.ADDITIONAL_FIELD + additionalFieldCount)
						.split(FileManagementConstant.PIPE_DELIMITER);
				additionalConfigField.setConfigName(additionalFields[0]);
				additionalConfigField.setConfigPos(additionalFields[1]);
				additionalConfigFieldList.add(additionalConfigField);
				additionalFieldCount++;
			}
		}
		return additionalConfigFieldList;
	}

	private FileConfigurationRest mapToFileConfigurationRest(FileConfigurationEntity configurationEntity) {

		FileConfigurationRest savedConfigurationRest = mapper.map(configurationEntity, FileConfigurationRest.class);
		FileConfigurationFields fileConfigurationFields = mapper.map(configurationEntity,
				FileConfigurationFields.class);
		savedConfigurationRest.setConfigurationFields(fileConfigurationFields);
		Map<String, String> confifMap = objectMapper.convertValue(configurationEntity, Map.class);
		if (confifMap.containsKey(FileManagementConstant.ADDITIONAL_FIELD_1)
				&& (confifMap.get(FileManagementConstant.ADDITIONAL_FIELD_1) != null)) {
			List<AdditionalConfigField> addConfigFieldList = getAdditionalConfigList(confifMap);
			savedConfigurationRest.getConfigurationFields().setAdditionalFieldList(addConfigFieldList);
		}
		return savedConfigurationRest;
	}
}