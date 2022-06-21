/**
 * created by supro
 */
package core.com.file.management.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import core.com.file.management.common.FileManagementConstant;
import core.com.file.management.model.AdditionalConfigField;

public class AbstractConfigurationService {

	@Autowired
	ObjectMapper objectMapper;

	public Map<String, String> populateEntityAdditionalFields(List<AdditionalConfigField> additionalConfigFieldList,
			Object fileConfigurationEntity) {
		Map<String, String> fileConfigEntityMap = objectMapper.convertValue(fileConfigurationEntity, Map.class);
		int additionalFieldCount = 1;
		while (additionalFieldCount <= 10) {
			fileConfigEntityMap.put(FileManagementConstant.ADDITIONAL_FIELD + additionalFieldCount++, null);
		}
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
		return fileConfigEntityMap;
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
