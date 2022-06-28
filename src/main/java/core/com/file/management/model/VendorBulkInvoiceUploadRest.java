package core.com.file.management.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "Bulk upload model", description = "This class is used to retrieve the uploaded file details")
@Data
public class VendorBulkInvoiceUploadRest {
	
	@ApiModelProperty(value = "File id")
	private String id;
	
	@ApiModelProperty(value = "IM Code")
	private String imCode;
	
	@ApiModelProperty(value = "Name")
	private String name;
	
	@ApiModelProperty(value = "Status")
	private String status;
	
	@ApiModelProperty(value = "Type")
	private String type;
	
	@ApiModelProperty(value = "File referencene id or GUID")
	private String guid;
	
	@ApiModelProperty(value = "Total invoices present")
	private Integer invoiceCount;
	
	@ApiModelProperty(value = "Total amount of the invoices ")
	private Double totalAmount;
	
	@ApiModelProperty(value = "File upload date")
	@JsonFormat(shape = JsonFormat.Shape.ANY, pattern="dd/MM/yyyy")
	private Date created;
	
}
