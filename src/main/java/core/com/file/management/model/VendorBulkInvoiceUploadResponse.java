package core.com.file.management.model;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class VendorBulkInvoiceUploadResponse {

	@ApiModelProperty(value = "Status")
	private String status;
	
	@ApiModelProperty(value = "Status code")
	private String status_code;
	
	@ApiModelProperty(value = "Status message")
	private String status_msg;
	
	@ApiModelProperty(value = "Medata regarding the list of files")
	private  ResponseMetadata metadata;
	
	@ApiModelProperty(value = "Records of the list of files")
	private List<VendorBulkInvoiceUploadRest> data;
	
}
