package core.com.file.management.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import core.com.file.management.mapper.DateDeSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ErrorInvoiceFileRest implements Serializable{

	private static final long serialVersionUID = 6025558677052568506L;
	
	@ApiModelProperty(value = "File id")
	private String fileId;
	
	@ApiModelProperty(value = "Reference no")
	private String referenceNo;
	
	@ApiModelProperty(value = "Creation time")
	@JsonFormat(shape = JsonFormat.Shape.ANY, pattern="dd/MM/yyyy")
	private Date creationTime;
	
	@JsonAlias("Invoice Number")
	@ApiModelProperty(value = "Invoice number")
	private String invoiceNumber;

	@JsonAlias("Invoice Amount")
	@ApiModelProperty(value = "Invoice amount")
	private String invoiceAmount;

	@JsonAlias("Invoice Date")
	@JsonDeserialize(using = DateDeSerializer.class)
	@JsonFormat(shape = JsonFormat.Shape.ANY, pattern="dd/MM/yyyy")
	@ApiModelProperty(value = "Invoice date")
	private Date invoiceDate;
	
	@JsonDeserialize(using = DateDeSerializer.class)
	@JsonFormat(shape = JsonFormat.Shape.ANY, pattern="dd/MM/yyyy")
	@ApiModelProperty(value = "Reversal date")
	private Date reversalDate;
	
	@JsonAlias("Vendor Code")
	@ApiModelProperty(value = "Vendor code")
	private String vendorCode;

	@JsonAlias("Vendor Name")
	@ApiModelProperty(value = "Vendor name")
	private String vendorName;

	@ApiModelProperty(value = "Status")
	private VendorInvoiceStatus status;

	@ApiModelProperty(value = "Status description")
	private String statusDescription;
	
	@ApiModelProperty(value = "Echeque no")
	private String echequeNo;
	
	@ApiModelProperty(value = "Additional field 1")
	private String additionalField1;
	
	@ApiModelProperty(value = "Additional field 2")
	private String additionalField2;
	
	@ApiModelProperty(value = "Additional field 3")
	private String additionalField3;
	
	@ApiModelProperty(value = "Additional field 4")
	private String additionalField4;
	
	@ApiModelProperty(value = "Additional field 5")
	private String additionalField5;
	
	@ApiModelProperty(value = "Additional field 6")
	private String additionalField6;
	
	@ApiModelProperty(value = "Additional field 7")
	private String additionalField7;
	
	@ApiModelProperty(value = "Additional field 8")
	private String additionalField8;
	
	@ApiModelProperty(value = "Additional field 9")
	private String additionalField9;
	
	@ApiModelProperty(value = "Additional field 10")
	private String additionalField10;
}
