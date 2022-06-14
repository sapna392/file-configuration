package core.com.file.management.validation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.fasterxml.jackson.databind.ObjectMapper;

import core.com.file.management.common.ErrorCode;
import core.com.file.management.common.FileManagementConstant;
import core.com.file.management.exception.FileConfigurationException;
import core.com.file.management.model.AdditionalConfigField;
import core.com.file.management.model.FileConfigurationRest;

@Component
public class FileConfigurationValidator implements Validator {

	public static final Logger LOGGER = LoggerFactory.getLogger(FileConfigurationValidator.class);

	@Autowired
	private ObjectMapper mapper;

	@Override
	public boolean supports(Class<?> clazz) {
		return FileConfigurationRest.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		LOGGER.info("Entering validate of " + FileConfigurationValidator.class.getName());

		FileConfigurationRest configurationRest = (FileConfigurationRest) target;
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userId", ErrorCode.EMPTY_USER_ID, "Invalid configuration");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userType", ErrorCode.EMPTY_USER_ID, "Invalid configuration");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "fileStructure", ErrorCode.INVALID_FILE_STRUCT,
				"Invalid configuration");
		if (FileManagementConstant.DELIMITER.equals(configurationRest.getFileStructure())) {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "delimiter", ErrorCode.DELIMITER_REQ,
					"Invalid configuration");
		}

		LOGGER.info("Exiting validate of " + FileConfigurationValidator.class.getName());
	}

	public void validateFileConfiguration(FileConfigurationRest configurationRest) throws FileConfigurationException {

		LOGGER.info("Entering validateFileConfiguration of " + FileConfigurationValidator.class.getName());

		Map<String, Object> configRestMap = mapper.convertValue(configurationRest.getConfigurationFields(), Map.class);
		if (configRestMap.containsKey(FileManagementConstant.ADDITIONAL_FIELD_LIST)) {
			configRestMap.remove(FileManagementConstant.ADDITIONAL_FIELD_LIST);
		}
		List<String> posList = configRestMap.values().stream()
				.collect(Collectors.mapping(pos -> (String) pos, Collectors.toList()));
		if (CollectionUtils.isNotEmpty(configurationRest.getConfigurationFields().getAdditionalFieldList())) {
			posList.addAll(configurationRest.getConfigurationFields().getAdditionalFieldList().stream()
					.map(AdditionalConfigField::getConfigPos).collect(Collectors.toList()));
		}

		if (FileManagementConstant.DELIMITER.equals(configurationRest.getFileStructure())) {
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
		} else if (FileManagementConstant.FIXED.equals(configurationRest.getFileStructure())) {
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

		LOGGER.info("Exiting validateFileConfiguration of " + FileConfigurationValidator.class.getName());
	}
}
