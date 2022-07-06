package core.com.file.management.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "Bulk upload model", description = "This class is used to retrieve the uploaded file details")
@Data
public class VendorBulkInvoiceUploadRest implements Serializable{
	
	private static final long serialVersionUID = -706458078534192123L;

	@ApiModelProperty(value = "File id")
	private String id;
	
	@ApiModelProperty(value = "IM Code")
	private String imCode;
	
	@ApiModelProperty(value = "Name")
	private String name;
	
	@ApiModelProperty(value = "Status")
	private VendorInvoiceStatus status;
	
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
	
	@ApiModelProperty(value = "File details along with the transaction details of the file")
	@JsonInclude(Include.NON_NULL)
	private List<VendorTxnInvoiceRest> vendorTxnInvoiceRestList;
	
	@ApiModelProperty(value = "Error details of the file")
	@JsonInclude(Include.NON_NULL)
	private VendorTxnInvoiceErrorRest vendorTxnInvoiceErrorRest;
	
}
