package core.com.file.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class VendorBulkUploadException extends Exception {

	private static final long serialVersionUID = 6307870797037610587L;

	VendorBulkUploadException() {
		super();
	}

	public VendorBulkUploadException(String message) {
		super(message);
	}

	public VendorBulkUploadException(String message, String placeholder) {
		super(String.format(message, placeholder));
	}

	public VendorBulkUploadException(String message, String placeholder1, String placeholder2) {
		super(String.format(message, placeholder1, placeholder2));
	}

	public VendorBulkUploadException(String message, Throwable cause) {
		super(message, cause);
	}

	public VendorBulkUploadException(Throwable cause) {
		super(cause);
	}
}
