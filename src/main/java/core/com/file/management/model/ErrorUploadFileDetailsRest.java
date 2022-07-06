package core.com.file.management.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ErrorUploadFileDetailsRest implements Serializable{

	private static final long serialVersionUID = 3866509535396986535L;

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
	
	@ApiModelProperty(value = "File upload date")
	@JsonFormat(shape = JsonFormat.Shape.ANY, pattern="dd/MM/yyyy")
	private Date created;
	
}
