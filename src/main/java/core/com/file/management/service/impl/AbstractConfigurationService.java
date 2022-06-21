/**
 * created by supro
 */
package core.com.file.management.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import core.com.file.management.common.ErrorCode;
import core.com.file.management.common.FileManagementConstant;
import core.com.file.management.exception.FileConfigurationException;
import core.com.file.management.model.AdditionalConfigField;

public class AbstractConfigurationService {

	@Autowired
	ObjectMapper objectMapper;

	public void populateEntityAdditionalFields(List<AdditionalConfigField> additionalConfigFieldList,
			Object fileConfigurationEntity) throws FileConfigurationException {
		Map<String, String> fileConfigEntityMap = new HashMap<>();
		int additionalFieldCount = 1;
		if (CollectionUtils.isNotEmpty(additionalConfigFieldList)) {
			additionalFieldCount = 1;
			for (AdditionalConfigField afl : additionalConfigFieldList) {
				StringBuilder sb = new StringBuilder();
				sb.append(afl.getConfigName());
				sb.append(FileManagementConstant.PIPE);
				sb.append(afl.getConfigPos());
				fileConfigEntityMap.put(FileManagementConstant.ADDITIONAL_FIELD + additionalFieldCount++,
						sb.toString());
			}
		}
		ObjectReader objectReader = objectMapper.readerForUpdating(fileConfigurationEntity);
		try {
			fileConfigurationEntity = objectReader
					.readValue(new ObjectMapper().writeValueAsString(fileConfigEntityMap));
		} catch (JsonProcessingException e) {
			throw new FileConfigurationException(ErrorCode.FILE_CONFIGURATION_ERROR);
		} 
	}

	public List<AdditionalConfigField> getAdditionalConfigList(Map<String, String> configMap) {
		List<AdditionalConfigField> additionalConfigFieldList = new ArrayList<>();
		int additionalFieldCount = 1;
		for (Map.Entry<String, String> entry : configMap.entrySet()) {
			if (entry.getKey().contains(FileManagementConstant.ADDITIONAL_FIELD + additionalFieldCount) && StringUtils
					.isNotBlank(configMap.get(FileManagementConstant.ADDITIONAL_FIELD + additionalFieldCount))) {
				AdditionalConfigField additionalConfigField = new AdditionalConfigField();
				String[] additionalFields = configMap
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

}
