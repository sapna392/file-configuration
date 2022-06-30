package core.com.file.management.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class VendorTxnInvoiceErrorRest {

	@ApiModelProperty("Invoice number")
	private String invoiceNumber;
	
	@ApiModelProperty(value = "Error description")
	private String errorDescription;
}
