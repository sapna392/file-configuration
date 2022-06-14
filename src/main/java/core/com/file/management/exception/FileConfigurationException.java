package core.com.file.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class FileConfigurationException extends Exception {

	private static final long serialVersionUID = -5757257191702094581L;

	FileConfigurationException() {
		super();
	}

	public FileConfigurationException(String message) {
		super(message);
	}
	
	public FileConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileConfigurationException(Throwable cause) {
		super(cause);
	}
}
