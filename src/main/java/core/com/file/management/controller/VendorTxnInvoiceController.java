package core.com.file.management.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import core.com.file.management.common.FileManagementConstant;
import core.com.file.management.model.VendorTxnInvoiceResponse;
import core.com.file.management.model.VendorTxnInvoiceRest;
import core.com.file.management.service.VendorTxnInvoiceService;
import core.com.file.management.util.FileConfigurationUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping(value = "/vendorInvoice")
@Api(value = "Vendor transaction invoice controller")
public class VendorTxnInvoiceController {

	@Autowired
	private VendorTxnInvoiceService vendorTxnInvoiceService;

	@Autowired
	private FileConfigurationUtil fileManagementUtil;

	@ApiOperation(value = "Upload bulk vendor invoice file")
	@GetMapping("/")
	public ResponseEntity<VendorTxnInvoiceResponse> getVendorTxnInvoiceDetails(
			@RequestParam(name = "imCode", required = true) String imCode,
			@RequestParam(name = "status", required = false) String status,
			@RequestParam(name = "dir", required = false) String dir,
			@RequestParam(name = "sortBy", required = false) String sortBy,
			@RequestParam(name = "size", required = false) Integer size,
			@RequestParam(name = "page", required = false) Integer page) {

		log.info("Entering getVendorTxnInvoiceDetails of {}", this.getClass().getSimpleName());

		Pageable pageable = fileManagementUtil.getPageable(dir, sortBy, size, page);
		VendorTxnInvoiceResponse vendorTxnInvoiceResponse = vendorTxnInvoiceService.getVendorTxnInvoiceDetails(pageable,
				status, imCode);

		log.info("Exiting getVendorTxnInvoiceDetails of {}", this.getClass().getSimpleName());
		return new ResponseEntity<VendorTxnInvoiceResponse>(vendorTxnInvoiceResponse,
				FileManagementConstant.SUCCESS.equals(vendorTxnInvoiceResponse.getStatus()) ? HttpStatus.OK
						: HttpStatus.BAD_REQUEST);
	}
	
	@PutMapping("/")
	public ResponseEntity<VendorTxnInvoiceResponse> authorizeTransaction(
			@RequestBody List<VendorTxnInvoiceRest> vendorTxnInvoiceRestList) {

		log.info("Entering authorizeTransaction of {}", this.getClass().getSimpleName());

		VendorTxnInvoiceResponse vendorTxnInvoiceResponse = new VendorTxnInvoiceResponse();
		vendorTxnInvoiceRestList = vendorTxnInvoiceService.authorizeTransaction(vendorTxnInvoiceRestList);
		vendorTxnInvoiceResponse.setData(vendorTxnInvoiceRestList);
		vendorTxnInvoiceResponse.setStatus_code(String.valueOf(HttpStatus.OK.value()));
		vendorTxnInvoiceResponse.setStatus(FileManagementConstant.SUCCESS);
		vendorTxnInvoiceResponse.setStatus_msg(FileManagementConstant.SUCCESS);

		log.info("Exiting authorizeTransaction of {}", this.getClass().getSimpleName());
		return new ResponseEntity<VendorTxnInvoiceResponse>(vendorTxnInvoiceResponse,
				FileManagementConstant.SUCCESS.equals(vendorTxnInvoiceResponse.getStatus()) ? HttpStatus.OK
						: HttpStatus.BAD_REQUEST);
	}

}
