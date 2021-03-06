package core.com.file.management.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class FileConfigurationFields implements Serializable {

	private static final long serialVersionUID = 4193168492078680894L;
	
	@ApiModelProperty(value = "Invoice number")
	private String invoiceNumber;

	@ApiModelProperty(value = "Invoice amount")
	private String invoiceAmount;

	@ApiModelProperty(value = "Invoice date")
	private String invoiceDate;

	@ApiModelProperty(value = "Vendor code")
	private String vendorCode;

	@ApiModelProperty(value = "Vendor name")
	private String vendorName;

	@ApiModelProperty(value = "Due date")
	private String dueDate;

	@ApiModelProperty(value = "Payment identifier")
	private String paymentIdentifier;
	
	@ApiModelProperty(value = "Processing date")
	private String processingDate;
	
	@ApiModelProperty(value = "List of additonal fields")
	List<AdditionalConfigField> additionalFieldList = new ArrayList<AdditionalConfigField>();

}
