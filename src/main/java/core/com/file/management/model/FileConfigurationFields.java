package core.com.file.management.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
public class FileConfigurationFields implements Serializable {

	private static final long serialVersionUID = 4193168492078680894L;
	
	@ApiParam(value = "Invoice number")
	private String invoiceNumber;

	@ApiParam(value = "Invoice amount")
	private String invoiceAmount;

	@ApiParam(value = "Invoice date")
	private String invoiceDate;

	@ApiParam(value = "Vendor code")
	private String vendorCode;

	@ApiParam(value = "Vendor name")
	private String vendorName;

	@ApiParam(value = "Due date")
	private String dueDate;

	@ApiParam(value = "Payment identifier")
	private String paymentIdentifier;
	
	@ApiParam(value = "Processing date")
	private String processingDate;
	
	@ApiParam(value = "List of additonal fields")
	List<AdditionalConfigField> additionalFieldList = new ArrayList<AdditionalConfigField>();

}
