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
@Api(value = "File configuration controller")
public class FileConfigurationController {

	@Autowired
	private FileConfigurationValidator validator;
	
	@Autowired
	private FileConfigurationService fileConfigurationService;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(validator);
	}

	@ApiOperation(value = "Submit file configuration")
	@PostMapping(value = "/submit")
	public ResponseEntity<FileConfigurationResponse> submitConfiguration(
			@Validated @RequestBody FileConfigurationRest fileConfigurationRest) {

		log.info("Entering bulkUpload of {}", this.getClass().getSimpleName());
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
		log.info("Exiting bulkUpload of {}", this.getClass().getSimpleName());
		return new ResponseEntity<>(configurationResponse, status);
	}

	@ApiOperation(value = "View file configuration")
	@GetMapping(value = "/view")
	public ResponseEntity<FileConfigurationResponse> viewConfiguration(
			@RequestParam(name = "imCode", required = true) String imCode) {

		log.info("Entering viewConfiguration of {}" ,this.getClass().getSimpleName());

		FileConfigurationResponse configurationResponse = new FileConfigurationResponse();
		FileConfigurationRest configurationRest = fileConfigurationService.viewFileConfiguration(imCode);
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
