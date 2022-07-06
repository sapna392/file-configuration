package core.com.file.management.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class VendorTxnInvoiceErrorRest {
	
	public VendorTxnInvoiceErrorRest(){}
	
	public VendorTxnInvoiceErrorRest(String fileName, String fileType){
		this.fileName = fileName;
		this.fileType = fileType;
	}

	@ApiModelProperty("File name")
	private String fileName;
	
	@ApiModelProperty(value = "File type")
	private String fileType;
	
	@ApiModelProperty(value = "Error content")
	private String fileContent;
}
