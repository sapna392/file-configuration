package core.com.file.management.validator;

import java.time.LocalDate;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import core.com.file.management.common.ErrorCode;
import core.com.file.management.common.FileManagementConstant;
import core.com.file.management.exception.VendorBulkUploadException;
import core.com.file.management.model.VendorBulkUploadRest;

@Component
public class VendorBulkUploadValidator {
	
	public void validateUploadedFile(MultipartFile file, String fileStructure) throws VendorBulkUploadException {
		
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
	}

	public void validateInvoiceDetails(VendorBulkUploadRest vendorBulkUploadRest) throws VendorBulkUploadException {

		if (vendorBulkUploadRest.getInvoiceNumber() == null) {
			throw new VendorBulkUploadException(ErrorCode.INVOICE_NUMBER_MISSING);
		}
		if (vendorBulkUploadRest.getInvoiceAmount() == null) {
			throw new VendorBulkUploadException(ErrorCode.MANDATORY_FIELD_MISSING, "Invoice amount",
					vendorBulkUploadRest.getInvoiceNumber());
		}
		if (vendorBulkUploadRest.getInvoiceDate() == null) {
			throw new VendorBulkUploadException(ErrorCode.MANDATORY_FIELD_MISSING, "Invoice date",
					vendorBulkUploadRest.getInvoiceNumber());
		}
		if (vendorBulkUploadRest.getVendorCode() == null) {
			throw new VendorBulkUploadException(ErrorCode.MANDATORY_FIELD_MISSING, "Vendor code",
					vendorBulkUploadRest.getInvoiceNumber());
		}
		if (vendorBulkUploadRest.getProcessingDate() == null) {
			throw new VendorBulkUploadException(ErrorCode.INVALID_DATE, "Processing date",
					vendorBulkUploadRest.getInvoiceNumber());
		}
		if (vendorBulkUploadRest.getDueDate() == null) {
			throw new VendorBulkUploadException(ErrorCode.INVALID_DATE, "Due date",
					vendorBulkUploadRest.getInvoiceNumber());
		}

		if (vendorBulkUploadRest.getProcessingDate().before(vendorBulkUploadRest.getInvoiceDate())) {
			throw new VendorBulkUploadException(ErrorCode.PROCESSING_DATE_EARLY,
					vendorBulkUploadRest.getInvoiceNumber());
		}
		
		if(vendorBulkUploadRest.getProcessingDate().before(java.sql.Date.valueOf(LocalDate.now()))) {
			throw new VendorBulkUploadException(ErrorCode.EARLIER_PROCESSING_DATE,
					vendorBulkUploadRest.getInvoiceNumber());
		}

	}

}
