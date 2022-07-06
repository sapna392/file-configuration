package core.com.file.management.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class VendorTxnInvoiceResponse implements Serializable{
	
	private static final long serialVersionUID = 2849496911202540026L;

	@ApiModelProperty(value = "Status")
	private String status;
	
	@ApiModelProperty(value = "Status code")
	private String status_code;
	
	@ApiModelProperty(value = "Status message")
	private String status_msg;
	
	@ApiModelProperty(value = "Medata regarding the list of files")
	private  ResponseMetadata metadata;
	
	@ApiModelProperty(value = "Bulk upload file details ")
	private VendorBulkInvoiceUploadRest data = new VendorBulkInvoiceUploadRest();
	
}
