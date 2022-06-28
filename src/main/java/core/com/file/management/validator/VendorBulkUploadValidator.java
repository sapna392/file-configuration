package core.com.file.management.validator;

import java.time.LocalDate;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import core.com.file.management.common.ErrorCode;
import core.com.file.management.common.FileManagementConstant;
import core.com.file.management.exception.VendorBulkUploadException;
import core.com.file.management.model.VendorTxnInvoiceRest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class VendorBulkUploadValidator {
	
	public void validateUploadedFile(MultipartFile file, String fileStructure) throws VendorBulkUploadException {
		
		log.info("Entering validateUploadedFile of {}", this.getClass().getSimpleName());
		
		if (!(FileManagementConstant.XLS_MIME_TYPE.equals(file.getContentType())
				|| FileManagementConstant.CSV_MIME_TYPE.equals(file.getContentType())
				|| FileManagementConstant.TXT_MIME_TYPE.equals(file.getContentType()))) {
			throw new VendorBulkUploadException(ErrorCode.INVALID_FILE_TYPE);
		}
		if (file.isEmpty()) {
			throw new VendorBulkUploadException(ErrorCode.EMPTY_FILE_CONTENT);
		}
		if ((FileManagementConstant.XLS_MIME_TYPE.equals(file.getContentType())
				|| FileManagementConstant.CSV_MIME_TYPE.equals(file.getContentType()))
						&& FileManagementConstant.FIXED.equals(fileStructure)) {
			throw new VendorBulkUploadException(ErrorCode.FILE_CONFIG_DOESNOT_MATCH);
		}
		
		log.info("Exiting validateUploadedFile of {}", this.getClass().getSimpleName());
	}

	public void validateInvoiceDetails(VendorTxnInvoiceRest vendorTxnInvoiceRest) throws VendorBulkUploadException {

		log.info("Entering validateInvoiceDetails of {}", this.getClass().getSimpleName());
		
		if (vendorTxnInvoiceRest.getInvoiceNumber() == null) {
			throw new VendorBulkUploadException(ErrorCode.INVOICE_NUMBER_MISSING);
		}
		if (vendorTxnInvoiceRest.getInvoiceAmount() == null) {
			throw new VendorBulkUploadException(ErrorCode.MANDATORY_FIELD_MISSING, "Invoice amount",
					vendorTxnInvoiceRest.getInvoiceNumber());
		}
		if (vendorTxnInvoiceRest.getInvoiceDate() == null) {
			throw new VendorBulkUploadException(ErrorCode.INVALID_DATE, "Invoice date",
					vendorTxnInvoiceRest.getInvoiceNumber());
		}
		if (vendorTxnInvoiceRest.getVendorCode() == null) {
			throw new VendorBulkUploadException(ErrorCode.MANDATORY_FIELD_MISSING, "Vendor code",
					vendorTxnInvoiceRest.getInvoiceNumber());
		}
		if (vendorTxnInvoiceRest.getProcessingDate() == null) {
			throw new VendorBulkUploadException(ErrorCode.INVALID_DATE, "Processing date",
					vendorTxnInvoiceRest.getInvoiceNumber());
		}
		if (vendorTxnInvoiceRest.getDueDate() == null) {
			throw new VendorBulkUploadException(ErrorCode.INVALID_DATE, "Due date",
					vendorTxnInvoiceRest.getInvoiceNumber());
		}

		if (vendorTxnInvoiceRest.getProcessingDate().before(vendorTxnInvoiceRest.getInvoiceDate())) {
			throw new VendorBulkUploadException(ErrorCode.PROCESSING_DATE_EARLY,
					vendorTxnInvoiceRest.getInvoiceNumber());
		}
		
		if(vendorTxnInvoiceRest.getProcessingDate().before(java.sql.Date.valueOf(LocalDate.now()))) {
			throw new VendorBulkUploadException(ErrorCode.EARLIER_PROCESSING_DATE,
					vendorTxnInvoiceRest.getInvoiceNumber());
		}

		log.info("Exiting validateInvoiceDetails of {}", this.getClass().getSimpleName());
	}

}
