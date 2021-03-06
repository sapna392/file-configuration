package core.com.file.management.controller;

import java.io.IOException;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import core.com.file.management.common.FileManagementConstant;
import core.com.file.management.exception.FileConfigurationException;
import core.com.file.management.exception.VendorBulkUploadException;
import core.com.file.management.model.ErrorUploadFileDetailsResponse;
import core.com.file.management.model.VendorBulkInvoiceUploadResponse;
import core.com.file.management.model.VendorBulkInvoiceUploadRest;
import core.com.file.management.model.VendorTxnInvoiceResponse;
import core.com.file.management.service.VendorBulkInvoiceUploadService;
import core.com.file.management.util.FileManagementUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping(value = "/bulkUpload")
@Api(value = "Vendor invoice bulk upload controller")
public class VendorBulkInvoiceUploadController {

	@Autowired
	VendorBulkInvoiceUploadService vendorBulkInvoiceUploadService;

	@Autowired
	FileManagementUtil fileManagementUtil;

	@ApiOperation(value = "Upload bulk vendor invoice file")
	@PostMapping("/")
	public ResponseEntity<VendorTxnInvoiceResponse> bulkUpload(@RequestParam("file") MultipartFile file,
			@RequestParam(name = "imCode", required = true) String imCode) {

		log.info("Entering bulkUpload of {}", this.getClass().getSimpleName());

		VendorTxnInvoiceResponse vendorTxnInvoiceResponse = new VendorTxnInvoiceResponse();
		VendorBulkInvoiceUploadRest vendorBulkInvoiceUploadRest = null;
		try {
			vendorBulkInvoiceUploadRest = vendorBulkInvoiceUploadService.upload(file, imCode);
			vendorTxnInvoiceResponse.setData(vendorBulkInvoiceUploadRest);
			if(CollectionUtils.isEmpty(vendorBulkInvoiceUploadRest.getVendorTxnInvoiceRestList())) {
				vendorTxnInvoiceResponse.setStatus(FileManagementConstant.FAILURE);
				vendorTxnInvoiceResponse.setStatus_code(String.valueOf(HttpStatus.BAD_REQUEST.value()));
				vendorTxnInvoiceResponse.setStatus_msg(FileManagementConstant.FILE_UPLOADED_FAILED);
			} else {
				vendorTxnInvoiceResponse.setStatus(FileManagementConstant.SUCCESS);
				vendorTxnInvoiceResponse.setStatus_code(String.valueOf(HttpStatus.OK.value()));
				vendorTxnInvoiceResponse.setStatus_msg(FileManagementConstant.FILE_UPLOADED_SUCCESS);
			}
		} catch (VendorBulkUploadException fce) {
			vendorTxnInvoiceResponse.setStatus_msg(fce.getMessage());
			vendorTxnInvoiceResponse.setStatus(FileManagementConstant.FAILURE);
			vendorTxnInvoiceResponse.setStatus_code(String.valueOf(HttpStatus.BAD_REQUEST.value()));
		}

		log.info(vendorTxnInvoiceResponse.toString());
		log.info("Exiting bulkUpload of {}", this.getClass().getSimpleName());

		return new ResponseEntity<>(vendorTxnInvoiceResponse,
				vendorBulkInvoiceUploadRest != null ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
	}

	@ApiOperation(value = "View details of the uploaded files")
	@GetMapping("/")
	public ResponseEntity<VendorBulkInvoiceUploadResponse> getFileUploadDetails(
			@RequestParam(name = "imCode", required = true) String imCode,
			@RequestParam(name = "status", required = false) String status,
			@RequestParam(name = "dir", required = false) String dir,
			@RequestParam(name = "sortBy", required = false) String sortBy,
			@RequestParam(name = "size", required = false) Integer size,
			@RequestParam(name = "page", required = false) Integer page) {

		log.info("Entering getFileUploadDetails of {}", this.getClass().getSimpleName());

		Pageable pageable = fileManagementUtil.getPageable(dir, sortBy, size, page);
		VendorBulkInvoiceUploadResponse fileResponse = vendorBulkInvoiceUploadService.getUploadFileDetails(pageable,
				status, imCode);

		log.info(fileResponse.toString());
		log.info("Exiting getFileUploadDetails of {}", this.getClass().getSimpleName());

		return new ResponseEntity<>(fileResponse,
				FileManagementConstant.SUCCESS.equals(fileResponse.getStatus()) ? HttpStatus.OK
						: HttpStatus.BAD_REQUEST);
	}

	@ApiOperation(value = "Download the file by file id")
	@GetMapping("/{fileId}")
	public ResponseEntity<Resource> getFileById(@RequestParam(name = "fileId") Long fileId,
			@RequestParam(name = "imCode", required = true) String imCode,
			@RequestParam(name = "isErrorFile") Boolean isErrorFile) {

		log.info("Entering getFileById of {}", this.getClass().getSimpleName());

		InputStreamResource resource;
		MultipartFile multipartFile = null;
		try {
			multipartFile = vendorBulkInvoiceUploadService.getUploadFileById(fileId, imCode, isErrorFile);
			resource = new InputStreamResource(multipartFile.getInputStream());
		} catch (VendorBulkUploadException | IOException e) {
			return ResponseEntity.internalServerError().body(null);
		}

		log.info("Exiting getFileById of {}", this.getClass().getSimpleName());

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, multipartFile.getName())
				.contentType(MediaType.parseMediaType(multipartFile.getContentType())).body(resource);
	}

	@ApiOperation(value = "Download the file template")
	@GetMapping(value = "/downloadTemplate")
	public ResponseEntity<Resource> downloadSampleFile(
			@RequestHeader(name = "Content-Type", required = true) final String mediaType,
			@RequestHeader(name = "Content-disposition", required = true) final String fileName,
			@RequestParam(name = "imCode", required = true) String imCode) throws FileConfigurationException {

		log.info("Entering downloadSampleFile of {}", this.getClass().getSimpleName());

		InputStreamResource resource;
		try {
			resource = new InputStreamResource(vendorBulkInvoiceUploadService.download(imCode, mediaType));
		} catch (VendorBulkUploadException e) {
			return ResponseEntity.internalServerError().body(null);
		}

		log.info("Exiting downLoadSampleFile of {}", this.getClass().getSimpleName());

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, fileName)
				.contentType(MediaType.parseMediaType(mediaType)).body(resource);
	}
	
	@ApiOperation(value = "Get reverse or error file details")
	@GetMapping("/reverseFile")
	public ResponseEntity<ErrorUploadFileDetailsResponse> getReverseFileDetails(
			@RequestParam(name = "imCode", required = true) String imCode,
			@RequestParam(name = "isErrorFile", required = true) Boolean isErrorFile,
			@RequestParam(name = "status", required = false) String status,
			@RequestParam(name = "dir", required = false) String dir,
			@RequestParam(name = "sortBy", required = false) String sortBy,
			@RequestParam(name = "size", required = false) Integer size,
			@RequestParam(name = "page", required = false) Integer page) {

		log.info("Entering getReverseFileDetails of {}", this.getClass().getSimpleName());

		Pageable pageable = fileManagementUtil.getPageable(dir, sortBy, size, page);
		ErrorUploadFileDetailsResponse uploadFileDetailsResponse = vendorBulkInvoiceUploadService
				.getReversalFileDetails(pageable, status, imCode, isErrorFile);

		log.info(uploadFileDetailsResponse.toString());
		log.info("Exiting getReverseFileDetails of {}", this.getClass().getSimpleName());

		return new ResponseEntity<>(uploadFileDetailsResponse,
				FileManagementConstant.SUCCESS.equals(uploadFileDetailsResponse.getStatus()) ? HttpStatus.OK
						: HttpStatus.BAD_REQUEST);
	}
	
}
