package core.com.file.management.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import core.com.file.management.common.ErrorCode;
import core.com.file.management.common.FileManagementConstant;
import core.com.file.management.model.ReverseFileConfigurationRest;

@Component
public class ReverseFileConfigurationValidator extends ConfigurationValidator implements Validator{

	public static final Logger LOGGER = LoggerFactory.getLogger(FileConfigurationValidator.class);
	
	@Override
	public boolean supports(Class<?> clazz) {
		return ReverseFileConfigurationRest.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		LOGGER.info("Entering validate of " + ReverseFileConfigurationValidator.class.getName());
		
		ReverseFileConfigurationRest configurationRest = (ReverseFileConfigurationRest) target;
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "imCode", ErrorCode.EMPTY_IM_CODE, "Invalid configuration");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "fileStructure", ErrorCode.INVALID_FILE_STRUCT,
				"Invalid configuration");
		if (FileManagementConstant.DELIMITER.equals(configurationRest.getFileStructure())) {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "fileDelimiter", ErrorCode.DELIMITER_REQ,
					"Invalid configuration");
		}
		ValidationUtils.rejectIfEmpty(errors, "configurationFields", ErrorCode.FILE_CONF_REQ, "Invalid configuration");
		
		LOGGER.info("Exiting validate of " + ReverseFileConfigurationValidator.class.getName());
	}

}
