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
import core.com.file.management.exception.NotFoundException;
import core.com.file.management.model.FileConfigurationResponse;
import core.com.file.management.model.FileConfigurationRest;
import core.com.file.management.service.FileConfigurationService;
import core.com.file.management.validator.FileConfigurationValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping(value = "/fileConfiguration")
@Api(value = "File Configuration Controller")
public class FileConfigurationController {

	@Autowired
	private FileConfigurationValidator validator;
	
	@Autowired
	private FileConfigurationService fileConfigurationService;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(validator);
	}

	@ApiOperation(value = "Submit file configuration", notes = "This method submits the file configuration")
	@PostMapping(value = "/submit")
	public ResponseEntity<FileConfigurationResponse> submitConfiguration(
			@Validated @RequestBody FileConfigurationRest fileConfigurationRest) {

		log.info("Entering submitConfiguration of " + FileConfigurationController.class.getName());
		log.info(fileConfigurationRest.toString());

		FileConfigurationResponse configurationResponse = new FileConfigurationResponse();
		FileConfigurationRest configurationRest= null;
		HttpStatus status = null;
		try {
			validator.validateFileConfiguration(fileConfigurationRest);
			configurationRest = fileConfigurationService.saveFileConfiguration(fileConfigurationRest);
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
		log.info("Exiting submitConfiguration of " + FileConfigurationController.class.getName());
		
		return new ResponseEntity<FileConfigurationResponse>(configurationResponse, status);
	}

	@ApiOperation(value = "View file configuration", notes = "This method shows the file configuration")
	@GetMapping(value = "view")
	public ResponseEntity<FileConfigurationResponse> viewConfiguration(
			@RequestParam(name = "imCode", required = true) String imCode) {

		log.info("Entering viewConfiguration of " + FileConfigurationController.class.getName());

		FileConfigurationResponse configurationResponse = new FileConfigurationResponse();
		FileConfigurationRest fileConfigurationRest = null;
		HttpStatus status = null;
		try {
			fileConfigurationRest = fileConfigurationService.viewFileConfiguration(imCode);
			configurationResponse.setStatus(FileManagementConstant.SUCCESS);
			configurationResponse.setStatus_code(String.valueOf(HttpStatus.OK.value()));
			configurationResponse.setStatus_msg(FileManagementConstant.FILE_CONFIG_FETCH_SUCCESS);
			configurationResponse.setData(fileConfigurationRest);
			status = HttpStatus.OK;
		} catch (NotFoundException e) {
			configurationResponse.setStatus(FileManagementConstant.FAILURE);
			configurationResponse.setStatus_code(String.valueOf(HttpStatus.NOT_FOUND.value()));
			configurationResponse.setStatus_msg(e.getMessage());
			configurationResponse.setData(fileConfigurationRest);
			status = HttpStatus.NOT_FOUND;
		}

		log.info("Entering viewConfiguration of " + FileConfigurationController.class.getName());
		
		return new ResponseEntity<FileConfigurationResponse>(configurationResponse, status);
	}
	
}
