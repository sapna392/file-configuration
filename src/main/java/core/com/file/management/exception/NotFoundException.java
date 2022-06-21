package core.com.file.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundException extends Exception {

	private static final long serialVersionUID = 1637440139102495278L;

	NotFoundException() {
		super();
	}

	public NotFoundException(String message) {
		super(message);
	}
	
	public NotFoundException(String message, String placeholder) {
		super(String.format(message, placeholder));
	}
	
	public NotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotFoundException(Throwable cause) {
		super(cause);
	}
}
