package core.com.file.management.validator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

import org.springframework.stereotype.Component;

import core.com.file.management.common.ErrorCode;
import core.com.file.management.exception.VendorBulkUploadException;
import core.com.file.management.model.VendorTxnInvoiceRest;

@Component
public class VendorTxnInvoiceValidator {

	public void validateUploadedFile(VendorTxnInvoiceRest vendorTxnInvcRest) throws VendorBulkUploadException {

		if (vendorTxnInvcRest.getInvoiceNumber() == null) {
			throw new VendorBulkUploadException(ErrorCode.INVOICE_NUMBER_MISSING);
		}
		if (vendorTxnInvcRest.getInvoiceAmount() == null) {
			throw new VendorBulkUploadException(ErrorCode.MANDATORY_FIELD_MISSING, "Invoice amount",
					vendorTxnInvcRest.getInvoiceNumber());
		}
		if (vendorTxnInvcRest.getInvoiceDate() == null) {
			throw new VendorBulkUploadException(ErrorCode.MANDATORY_FIELD_MISSING, "Invoice date",
					vendorTxnInvcRest.getInvoiceNumber());
		}
		if (vendorTxnInvcRest.getVendorCode() == null) {
			throw new VendorBulkUploadException(ErrorCode.MANDATORY_FIELD_MISSING, "Vendor code",
					vendorTxnInvcRest.getInvoiceNumber());
		}
		if (vendorTxnInvcRest.getProcessingDate() == null) {
			throw new VendorBulkUploadException(ErrorCode.MANDATORY_FIELD_MISSING, "Processing date",
					vendorTxnInvcRest.getInvoiceNumber());
		}
		if (vendorTxnInvcRest.getDueDate() == null) {
			throw new VendorBulkUploadException(ErrorCode.MANDATORY_FIELD_MISSING, "Due date",
					vendorTxnInvcRest.getInvoiceNumber());
		}

		if (vendorTxnInvcRest.getProcessingDate().before(vendorTxnInvcRest.getInvoiceDate())) {
			throw new VendorBulkUploadException(ErrorCode.PROCESSING_DATE_EARLY, vendorTxnInvcRest.getInvoiceNumber());
		}
		
		if(vendorTxnInvcRest.getProcessingDate().before(java.sql.Date.valueOf(LocalDate.now()))) {
			throw new VendorBulkUploadException(ErrorCode.EARLIER_PROCESSING_DATE, vendorTxnInvcRest.getInvoiceNumber());
		}

	}

}
