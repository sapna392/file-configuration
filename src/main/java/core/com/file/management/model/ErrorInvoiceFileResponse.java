package core.com.file.management.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;

public class ErrorInvoiceFileResponse implements Serializable{

	private static final long serialVersionUID = -1576184242711963896L;

	@ApiModelProperty(value = "Status")
	private String status;
	
	@ApiModelProperty(value = "Status code")
	private String status_code;
	
	@ApiModelProperty(value = "Status message")
	private String status_msg;
	
	@ApiModelProperty(value = "Medata regarding the list of files")
	private  ResponseMetadata metadata;
	
	@ApiModelProperty(value = "Reverse file details ")
	private ErrorInvoiceFileRest errorInvoiceFileRest;
	
}
