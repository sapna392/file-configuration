package core.com.file.management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import core.com.file.management.common.ErrorCode;
import core.com.file.management.common.FileManagementConstant;
import core.com.file.management.exception.FileConfigurationException;
import core.com.file.management.model.ErrorFileConfigurationResponse;
import core.com.file.management.model.ErrorFileConfigurationRest;
import core.com.file.management.service.ErrorFileConfigurationService;
import core.com.file.management.validator.ErrorFileConfigurationValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping(value = "/errorFileConfiguration")
@Api(value = "Error file configuration controller")
public class ErrorFileConfigurationController {
	
	@Autowired
	private ErrorFileConfigurationValidator validator;
	
	@Autowired
	private ErrorFileConfigurationService errorFileConfigurationService;
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(validator);
	}
	
	@ApiOperation(value = "Submit error file configuration")
	@PostMapping("/submit")
	public ResponseEntity<ErrorFileConfigurationResponse> submitConfiguration(
			@Validated @RequestBody ErrorFileConfigurationRest errorFileConfigurationRest) {
		
		log.info("Entering submitConfiguration of {}" ,this.getClass().getSimpleName());
		log.info(errorFileConfigurationRest.toString());
		
		ErrorFileConfigurationResponse configurationResponse = new ErrorFileConfigurationResponse();
		ErrorFileConfigurationRest configurationRest = null;
		HttpStatus status = null;
		
		try {
			validator.validateFileConfiguration(errorFileConfigurationRest);
			configurationRest = errorFileConfigurationService.saveFileConfiguration(errorFileConfigurationRest);
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

		log.info(configurationResponse.toString());
		log.info("Exiting submitConfiguration of {}" ,this.getClass().getSimpleName());
		return new ResponseEntity<>(configurationResponse, status);
	}
	
	@ApiOperation(value = "View error file configuration")
	@GetMapping("/view")
	public ResponseEntity<ErrorFileConfigurationResponse> viewConfiguration(
			@RequestParam(name = "imCode", required = true) String imCode) {

		log.info("Entering viewConfiguration of {}" ,this.getClass().getSimpleName());

		ErrorFileConfigurationResponse configurationResponse = new ErrorFileConfigurationResponse();
		ErrorFileConfigurationRest configurationRest = errorFileConfigurationService.viewFileConfiguration(imCode);
		configurationResponse.setData(configurationRest);
		configurationResponse.setStatus(FileManagementConstant.SUCCESS);
		configurationResponse.setStatus_code(String.valueOf(HttpStatus.OK.value()));
		if(configurationRest != null) {
			configurationResponse.setStatus_msg(FileManagementConstant.FILE_CONFIG_FETCH_SUCCESS);
		} else {
			configurationResponse.setStatus_msg(FileManagementConstant.FILE_CONFIG_DOESNOT_EXISTS);
		}
		

		log.info(configurationResponse.toString());
		log.info("Exiting viewConfiguration of {}" ,this.getClass().getSimpleName());
		return new ResponseEntity<>(configurationResponse, HttpStatus.OK);
	}

}
