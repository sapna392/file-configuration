package core.com.file.management.validator;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import core.com.file.management.common.ErrorCode;
import core.com.file.management.common.FileManagementConstant;
import core.com.file.management.exception.FileConfigurationException;
import core.com.file.management.model.AdditionalConfigField;
import core.com.file.management.model.ErrorFileConfigurationRest;
import core.com.file.management.model.FileConfigurationRest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ConfigurationValidator {

	@Autowired
	private ObjectMapper mapper;

	public void validateFileConfiguration(Object configurationRest) throws FileConfigurationException {

		log.info("Entering validateFileConfiguration of {}", this.getClass().getSimpleName());

		Object configurationField = null;
		List<AdditionalConfigField> additionalFieldList = null;
		String fileStructure = null;

		if (configurationRest instanceof FileConfigurationRest) {
			configurationField = ((FileConfigurationRest) configurationRest).getConfigurationFields();
			additionalFieldList = ((FileConfigurationRest) configurationRest).getConfigurationFields()
					.getAdditionalFieldList();
			fileStructure = ((FileConfigurationRest) configurationRest).getFileStructure();
		} else if (configurationRest instanceof ErrorFileConfigurationRest) {
			configurationField = ((ErrorFileConfigurationRest) configurationRest).getConfigurationFields();
			additionalFieldList = ((ErrorFileConfigurationRest) configurationRest).getConfigurationFields()
					.getAdditionalFieldList();
			fileStructure = ((ErrorFileConfigurationRest) configurationRest).getFileStructure();
		}

		Map<String, Object> configRestMap = mapper.convertValue(configurationField, Map.class);
		if (configRestMap.containsKey(FileManagementConstant.ADDITIONAL_FIELD_LIST)) {
			configRestMap.remove(FileManagementConstant.ADDITIONAL_FIELD_LIST);
		}
		List<String> posList = configRestMap.values().stream()
				.collect(Collectors.mapping(pos -> (String) pos, Collectors.toList()));
		if (CollectionUtils.isNotEmpty((Collection<?>) additionalFieldList)) {
			posList.addAll(((Collection<AdditionalConfigField>) additionalFieldList).stream()
					.map(AdditionalConfigField::getConfigPos).collect(Collectors.toList()));
		}

		if (FileManagementConstant.DELIMITER.equals(fileStructure)) {
			List<Integer> intPosList = posList.stream().map(pos -> {
				try {
					return Integer.parseInt(pos);
				} catch (NumberFormatException nfe) {
					return 0;
				}
			}).collect(Collectors.toList());

			if (intPosList.contains(0)) {
				throw new FileConfigurationException(ErrorCode.INVALID_POSITION);
			}
			int max = intPosList.stream().mapToInt(pos -> pos).max().getAsInt();
			if (max != intPosList.size()) {
				throw new FileConfigurationException(ErrorCode.INVALID_ORDER);
			}
		} else if (FileManagementConstant.FIXED.equals(fileStructure)) {
			List<Integer> intValueList = posList.stream()
					.map(vl -> Arrays.asList(vl.split(FileManagementConstant.COMMA)).stream().map(p -> {
						try {
							return Integer.parseInt(p);
						} catch (NumberFormatException nfe) {
							return -1;
						}
					})).flatMap(c -> c).sorted().distinct().collect(Collectors.toList());
			if (intValueList.contains(-1) || intValueList.size() / 2 != posList.size()) {
				throw new FileConfigurationException(ErrorCode.INVALID_ORDER);
			}

		} else {
			throw new FileConfigurationException(ErrorCode.INVALID_FILE_STRUCT);
		}

		log.info("Exiting validateFileConfiguration of {}", this.getClass().getSimpleName());
	}

}
