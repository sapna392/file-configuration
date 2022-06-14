package core.com.file.management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import core.com.file.management.exception.FileConfigurationException;
import core.com.file.management.model.BulkUploadFileResponse;
import core.com.file.management.model.BulkUploadFileRest;
import core.com.file.management.service.VendorBulkUploadFileService;
import core.com.file.management.util.FileManagementUtil;
import core.com.file.management.validation.VendorTxnInvoiceValidator;

@RestController
@RequestMapping(value = "/fileManagement")
public class FileManagementController {

	@Autowired
	VendorTxnInvoiceValidator validator;

	@Autowired
	VendorBulkUploadFileService bulkUploadService;
	
	@Autowired
	FileManagementUtil fileManagementUtil;

	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
		
		String response = null;
		/*try {
			response = bulkUploadService.upload(file);
		} catch(FileConfigurationException fce) {
			response = fce.getMessage();
		}*/
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ResponseEntity<BulkUploadFileResponse> getFileUploadDetails(@RequestParam(name = "imCode") String imCode,
			@RequestParam(name = "status", required = false) String status,
			@RequestParam(name = "dir", required = false) String dir,
			@RequestParam(name = "sortBy", required = false) String sortBy,
			@RequestParam(name = "size", required = false) Integer size,
			@RequestParam(name = "page", required = false) Integer page) {

		Pageable pageable = fileManagementUtil.getPageable(dir, sortBy, size, page);
		BulkUploadFileResponse respone = bulkUploadService.getUploadFileDetails(pageable, status, imCode);

		return new ResponseEntity<BulkUploadFileResponse>(respone, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/{fileId}", method = RequestMethod.GET)
	public ResponseEntity<BulkUploadFileRest> getFileById(@RequestParam String fileId){
		
		
		return null;
	}
	

}
