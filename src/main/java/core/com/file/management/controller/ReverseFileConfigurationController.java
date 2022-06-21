package core.com.file.management.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import core.com.file.management.common.ErrorCode;
import core.com.file.management.common.FileManagementConstant;
import core.com.file.management.exception.FileConfigurationException;
import core.com.file.management.exception.NotFoundException;
import core.com.file.management.model.ReverseFileConfigurationResponse;
import core.com.file.management.model.ReverseFileConfigurationRest;
import core.com.file.management.service.ReverseFileConfigurationService;
import core.com.file.management.validator.ReverseFileConfigurationValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@CrossOrigin
@RequestMapping(value = "/reverseFileConfiguration")
@Api(value = "Reverse File Configuration Controller")
public class ReverseFileConfigurationController {
	
	public static final Logger LOGGER = LoggerFactory.getLogger(ReverseFileConfigurationController.class);
	
	@Autowired
	private ReverseFileConfigurationValidator validator;
	
	@Autowired
	private ReverseFileConfigurationService reverseFileConfigurationService;
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(validator);
	}
	
	@RequestMapping(value = "/submit", method = RequestMethod.POST)
	public ResponseEntity<ReverseFileConfigurationResponse> submitConfiguration(
			@Validated @RequestBody ReverseFileConfigurationRest reverseFileConfigurationRest) {
		
		LOGGER.info("Entering submitConfiguration of " +ReverseFileConfigurationController.class.getName());
		LOGGER.info(reverseFileConfigurationRest.toString());
		
		ReverseFileConfigurationResponse configurationResponse = new ReverseFileConfigurationResponse();
		ReverseFileConfigurationRest configurationRest = null;
		HttpStatus status = null;
		
		try {
			validator.validateFileConfiguration(reverseFileConfigurationRest);
			configurationRest = reverseFileConfigurationService.saveFileConfiguration(reverseFileConfigurationRest);
			configurationResponse.setData(configurationRest);
			configurationResponse.setStatus(FileManagementConstant.SUCCESS);
			configurationResponse.setStatus_code(String.valueOf(HttpStatus.OK.value()));
			configurationResponse.setStatus_msg(FileManagementConstant.FILE_CONFIG_SAVE_SUCCESS);
			status = HttpStatus.OK;
		} catch (FileConfigurationException e) {
			configurationResponse.setData(configurationRest);
			if (ErrorCode.INVALID_POSITION.equals(e.getMessage()) || ErrorCode.INVALID_ORDER.equals(e.getMessage())
					|| ErrorCode.INVALID_FILE_STRUCT.equals(e.getMessage())) {
				configurationResponse.setStatus_code(String.valueOf(HttpStatus.BAD_REQUEST.value()));
				status = HttpStatus.BAD_REQUEST;
			} else {
				configurationResponse.setStatus_code(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
				status = HttpStatus.INTERNAL_SERVER_ERROR;
			}
			configurationResponse.setStatus(FileManagementConstant.FAILURE);
			configurationResponse.setStatus_msg(e.getMessage());
		}

		LOGGER.info(configurationResponse.toString());
		LOGGER.info("Exiting submitConfiguration of " + ReverseFileConfigurationController.class.getName());
		return new ResponseEntity<ReverseFileConfigurationResponse>(configurationResponse, status);
	}
	
	@ApiOperation(value = "View file configuration", notes = "This method shows the file configuration")
	@RequestMapping(value = "view", method = RequestMethod.GET)
	public ResponseEntity<ReverseFileConfigurationResponse> viewConfiguration(
			@RequestParam(name = "userId", required = true) String userId) {

		LOGGER.info("Entering viewConfiguration of " + FileConfigurationController.class.getName());

		ReverseFileConfigurationResponse configurationResponse = new ReverseFileConfigurationResponse();
		ReverseFileConfigurationRest configurationRest = null;
		HttpStatus status = null;
		try {
			configurationRest = reverseFileConfigurationService.viewFileConfiguration(userId);
			configurationResponse.setStatus(FileManagementConstant.SUCCESS);
			configurationResponse.setStatus_code(String.valueOf(HttpStatus.OK.value()));
			if(configurationRest != null) {
				configurationResponse.setStatus_msg(FileManagementConstant.FILE_CONFIG_FETCH_SUCCESS);
			} else {
				configurationResponse.setStatus_msg(FileManagementConstant.FILE_CONFIG_DOESNOT_EXISTS);
			}
			
			configurationResponse.setData(configurationRest);
			status = HttpStatus.OK;
		} catch (NotFoundException nfe) {
			configurationResponse.setStatus(FileManagementConstant.FAILURE);
			configurationResponse.setStatus_code(String.valueOf(HttpStatus.NOT_FOUND.value()));
			configurationResponse.setStatus_msg(nfe.getMessage());
			configurationResponse.setData(configurationRest);
			status = HttpStatus.NOT_FOUND;
		}

		LOGGER.info("Entering viewConfiguration of " + FileConfigurationController.class.getName());
		return new ResponseEntity<ReverseFileConfigurationResponse>(configurationResponse, status);
	}

}
