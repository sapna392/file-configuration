package core.com.file.management.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import core.com.file.management.common.FileManagementConstant;
import core.com.file.management.exception.FileConfigurationException;
import core.com.file.management.exception.VendorBulkUploadException;
import core.com.file.management.model.BulkUploadFileResponse;
import core.com.file.management.service.VendorBulkUploadFileService;
import core.com.file.management.util.FileConfigurationUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping(value = "bulkUpload")
public class VendorBulkUploadController {
	
	@Autowired
	VendorBulkUploadFileService vendorBulkUploadService;
	
	@Autowired
	FileConfigurationUtil fileManagementUtil;
	
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ResponseEntity<BulkUploadFileResponse> bulkUpload(@RequestParam("file") MultipartFile file,
			@RequestParam(name = "imCode", required = true) String imCode) {
		
		log.info("Entering bulkUpload of {}", this.getClass().getSimpleName());
		
		BulkUploadFileResponse bulkUploadFileResponse = new BulkUploadFileResponse();
		String response = null;
		HttpStatus status = null;
		try {
			response = vendorBulkUploadService.upload(file, imCode);
			bulkUploadFileResponse.setStatus_code(HttpStatus.OK.value());
			bulkUploadFileResponse.setStatus(FileManagementConstant.SUCCESS);
			bulkUploadFileResponse.setStatus_msg(response);
			status = HttpStatus.OK;
		} catch(VendorBulkUploadException fce) {
			response = fce.getMessage();
			bulkUploadFileResponse.setStatus_code(HttpStatus.BAD_REQUEST.value());
			bulkUploadFileResponse.setStatus(FileManagementConstant.FAILURE);
			bulkUploadFileResponse.setStatus_msg(response);
			status = HttpStatus.BAD_REQUEST;
		}
		
		log.info("Exiting bulkUpload of {}", this.getClass().getSimpleName());
		
		return new ResponseEntity<BulkUploadFileResponse>(bulkUploadFileResponse, status);
	}
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ResponseEntity<BulkUploadFileResponse> getFileUploadDetails(@RequestParam(name = "imCode", required = true) String imCode,
			@RequestParam(name = "status", required = false) String status,
			@RequestParam(name = "dir", required = false) String dir,
			@RequestParam(name = "sortBy", required = false) String sortBy,
			@RequestParam(name = "size", required = false) Integer size,
			@RequestParam(name = "page", required = false) Integer page) {
		
		log.info("Entering getFileUploadDetails of {}", this.getClass().getSimpleName());

		Pageable pageable = fileManagementUtil.getPageable(dir, sortBy, size, page);
		BulkUploadFileResponse respone = vendorBulkUploadService.getUploadFileDetails(pageable, status, imCode);
		
		log.info("Exiting getFileUploadDetails of {}", this.getClass().getSimpleName());

		return new ResponseEntity<BulkUploadFileResponse>(respone, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/{fileId}", method = RequestMethod.GET)
	public ResponseEntity<Resource> getFileById(@RequestParam(name = "fileId") Long fileId,
			@RequestParam(name = "userId", required = true) String userId,
			@RequestParam(name = "userType", required = true) String userType) {
		
		log.info("Entering getFileById of {}", this.getClass().getSimpleName());
		
		InputStreamResource resource;
		MultipartFile multipartFile = null;
		try {
			multipartFile = vendorBulkUploadService.getUploadFileById(fileId, userId, userType);
			resource = new InputStreamResource(multipartFile.getInputStream());
		} catch (VendorBulkUploadException | IOException e) {
			return ResponseEntity.internalServerError().body(null);
		}
		
		log.info("Exiting getFileById of {}", this.getClass().getSimpleName());
		
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, multipartFile.getName())
				.contentType(MediaType.parseMediaType(multipartFile.getContentType())).body(resource);
	}
	
	@RequestMapping(value = "/downloadTemplate")
	public ResponseEntity<Resource> downLoadSampleFile(
			@RequestHeader(name = "Content-Type", required = true) final String mediaType,
			@RequestHeader(name = "Content-disposition", required = true) final String fileName,
			@RequestParam(name = "userId", required = true) String userId,
			@RequestParam(name = "userType", required = true) String userType) throws FileConfigurationException {

		log.info("Entering downLoadSampleFile of {}", this.getClass().getSimpleName());
		
		InputStreamResource resource;
		try {
			resource = new InputStreamResource(vendorBulkUploadService.download(userId, userType, mediaType));
		} catch (VendorBulkUploadException e) {
			return ResponseEntity.internalServerError().body(null);
		}
		
		log.info("Exiting downLoadSampleFile of {}", this.getClass().getSimpleName());
		
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, fileName)
				.contentType(MediaType.parseMediaType(mediaType)).body(resource);
	}

}
