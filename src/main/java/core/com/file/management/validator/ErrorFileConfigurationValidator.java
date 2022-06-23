package core.com.file.management.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import core.com.file.management.common.ErrorCode;
import core.com.file.management.common.FileManagementConstant;
import core.com.file.management.model.ErrorFileConfigurationRest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ErrorFileConfigurationValidator extends ConfigurationValidator implements Validator{

	@Override
	public boolean supports(Class<?> clazz) {
		return ErrorFileConfigurationRest.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		log.info("Entering validate of {}", this.getClass().getSimpleName());
		
		ErrorFileConfigurationRest configurationRest = (ErrorFileConfigurationRest) target;
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "imCode", ErrorCode.EMPTY_IM_CODE, "Invalid configuration");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "fileStructure", ErrorCode.INVALID_FILE_STRUCT,
				"Invalid configuration");
		if (FileManagementConstant.DELIMITER.equals(configurationRest.getFileStructure())) {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "fileDelimiter", ErrorCode.DELIMITER_REQ,
					"Invalid configuration");
		}
		ValidationUtils.rejectIfEmpty(errors, "configurationFields", ErrorCode.FILE_CONF_REQ, "Invalid configuration");
		
		log.info("Exiting validate of {}", this.getClass().getSimpleName());
	}

}
