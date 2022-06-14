package core.com.file.management.model;

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import core.com.file.management.validation.DateDeSerializer;
import lombok.Data;

@Data
public class VendorTxnInvoiceRest {

	private String invoiceNumber;

	private Double invoiceAmount;

	@JsonDeserialize(using = DateDeSerializer .class)
	private Date invoiceDate;

	private String vendorCode;
	
	private String vendorName;

	@JsonDeserialize(using = DateDeSerializer .class)
	private Date dueDate;

	private String paymentIdentifier;
	
	@JsonDeserialize(using = DateDeSerializer .class)
	private Date processingDate;
	
	private String additionalField1;
	
	private String additionalField2;
	
	private String additionalField3;
	
	private String additionalField4;
	
	private String additionalField5;
	
	private String additionalField6;
	
	private String additionalField7;
	
	private String additionalField8;
	
	private String additionalField9;
	
	private String additionalField10;
	
}
