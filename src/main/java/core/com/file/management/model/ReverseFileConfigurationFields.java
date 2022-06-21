package core.com.file.management.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
public class ReverseFileConfigurationFields implements Serializable{

	private static final long serialVersionUID = 7379657464500924505L;

	@ApiParam(value = "File id")
	private String fileId;
	
	@ApiParam(value = "Reference no")
	private String referenceNo;
	
	@ApiParam(value = "Creation time")
	private String creationTime;
	
	@ApiParam(value = "Invoice number")
	private String invoiceNumber;

	@ApiParam(value = "Invoice amount")
	private String invoiceAmount;

	@ApiParam(value = "Invoice date")
	private String invoiceDate;
	
	@ApiParam(value = "Reversal date")
	private String reversalDate;
	
	@ApiParam(value = "Vendor code")
	private String vendorCode;

	@ApiParam(value = "Vendor name")
	private String vendorName;

	@ApiParam(value = "Status")
	private String status;

	@ApiParam(value = "Status description")
	private String statusDescription;
	
	@ApiParam(value = "Echeque no")
	private String echequeNo;
	
	@ApiParam(value = "List of additonal fields")
	List<AdditionalConfigField> additionalFieldList = new ArrayList<AdditionalConfigField>();
	
}
