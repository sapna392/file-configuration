package core.com.file.management.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ErrorFileConfigurationFields implements Serializable{

	private static final long serialVersionUID = 7379657464500924505L;

	@ApiModelProperty(value = "File id")
	private String fileId;
	
	@ApiModelProperty(value = "Reference no")
	private String referenceNo;
	
	@ApiModelProperty(value = "Creation time")
	private String creationTime;
	
	@ApiModelProperty(value = "Invoice number")
	private String invoiceNumber;

	@ApiModelProperty(value = "Invoice amount")
	private String invoiceAmount;

	@ApiModelProperty(value = "Invoice date")
	private String invoiceDate;
	
	@ApiModelProperty(value = "Reversal date")
	private String reversalDate;
	
	@ApiModelProperty(value = "Vendor code")
	private String vendorCode;

	@ApiModelProperty(value = "Vendor name")
	private String vendorName;

	@ApiModelProperty(value = "Status")
	private String status;

	@ApiModelProperty(value = "Status description")
	private String statusDescription;
	
	@ApiModelProperty(value = "Echeque no")
	private String echequeNo;
	
	@ApiModelProperty(value = "List of additonal fields")
	private List<AdditionalConfigField> additionalFieldList = new ArrayList<>();
	
}
