package core.com.file.management.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class VendorTxnInvoiceErrorRest implements Serializable{
	
	private static final long serialVersionUID = 3524488549351227616L;

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
