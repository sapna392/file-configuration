package core.com.file.management.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import core.com.file.management.common.ErrorCode;
import core.com.file.management.common.FileManagementConstant;
import core.com.file.management.exception.FileConfigurationException;
import core.com.file.management.model.FileConfigurationResponse;
import core.com.file.management.model.FileConfigurationRest;
import core.com.file.management.service.FileConfigurationService;
import core.com.file.management.validation.FileConfigurationValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@CrossOrigin
@RequestMapping(value = "/fileConfiguration")
@Api(value = "File Configuration Controller")
public class FileConfigurationController {

	public static final Logger LOGGER = LoggerFactory.getLogger(FileConfigurationController.class);
	@Autowired
	private FileConfigurationService fileConfigurationService;

	@Autowired
	private FileConfigurationValidator validator;

	@InitBinder("uploadFile")
	public void initMerchantOnlyBinder(WebDataBinder binder) {
		binder.addValidators(validator);
	}

	@ApiOperation(value = "Submit file configuration", notes = "This method submits the file configuration")
	@RequestMapping(value = "/submit", method = RequestMethod.POST)
	public ResponseEntity<FileConfigurationResponse> submitConfiguration(
			@Validated @RequestBody FileConfigurationRest fileConfigurationRest) {

		LOGGER.info("Entering submitConfiguration of " + FileConfigurationController.class.getName());
		LOGGER.info(fileConfigurationRest.toString());

		FileConfigurationResponse configurationResponse = new FileConfigurationResponse();
		FileConfigurationRest configurationRest= null;
		try {
			validator.validateFileConfiguration(fileConfigurationRest);
			configurationRest = fileConfigurationService.saveFileConfiguration(fileConfigurationRest);
			configurationResponse.setData(configurationRest);
			configurationResponse.setStatus(FileManagementConstant.SUCCESS);
			configurationResponse.setStatus_code(String.valueOf(HttpStatus.OK.value()));
			configurationResponse.setStatus_msg(FileManagementConstant.FILE_CONFIG_SAVE_SUCCESS);
		} catch (FileConfigurationException e) {
			configurationResponse.setData(configurationRest);
			if (ErrorCode.INVALID_POSITION.equals(e.getMessage()) || ErrorCode.INVALID_ORDER.equals(e.getMessage())
					|| ErrorCode.INVALID_FILE_STRUCT.equals(e.getMessage())) {
				configurationResponse.setStatus_code(String.valueOf(HttpStatus.BAD_REQUEST.value()));
			} else {
				configurationResponse.setStatus_code(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
			}
			configurationResponse.setStatus(FileManagementConstant.FAILURE);
			configurationResponse.setStatus_msg(e.getMessage());
		}

		LOGGER.info(configurationResponse.toString());
		LOGGER.info("Exiting submitConfiguration of " + FileConfigurationController.class.getName());
		return new ResponseEntity<FileConfigurationResponse>(configurationResponse, HttpStatus.OK);
	}

	@ApiOperation(value = "View file configuration", notes = "This method shows the file configuration")
	@RequestMapping(value = "view", method = RequestMethod.GET)
	public ResponseEntity<FileConfigurationResponse> viewConfiguration(
			@RequestParam(name = "userId", required = true) String userId,
			@RequestParam(name = "userType", required = true) String userType) {

		LOGGER.info("Entering viewConfiguration of " + FileConfigurationController.class.getName());

		FileConfigurationResponse configurationResponse = new FileConfigurationResponse();
		FileConfigurationRest fileConfigurationRest = null;
		try {
			fileConfigurationRest = fileConfigurationService.viewFileConfiguration(userId, userType);
			configurationResponse.setStatus(FileManagementConstant.SUCCESS);
			configurationResponse.setStatus_code(String.valueOf(HttpStatus.OK.value()));
			configurationResponse.setStatus_msg(FileManagementConstant.FILE_CONFIG_FETCH_SUCCESS);
			configurationResponse.setData(fileConfigurationRest);
		} catch (FileConfigurationException e) {
			configurationResponse.setStatus(FileManagementConstant.FAILURE);
			configurationResponse.setStatus_code(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
			configurationResponse.setStatus_msg(e.getMessage());
			configurationResponse.setData(fileConfigurationRest);
		}

		LOGGER.info("Entering viewConfiguration of " + FileConfigurationController.class.getName());
		return new ResponseEntity<FileConfigurationResponse>(configurationResponse, HttpStatus.OK);
	}
	
}
