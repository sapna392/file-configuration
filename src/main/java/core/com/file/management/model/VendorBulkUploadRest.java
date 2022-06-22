package core.com.file.management.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import core.com.file.management.mapper.DateDeSerializer;
import core.com.file.management.mapper.DoubleDeSerializer;
import lombok.Data;

@Data
public class VendorBulkUploadRest {

	@JsonAlias("InvoiceNumber")
	private String invoiceNumber;

	@JsonAlias("InvoiceAmount")
	@JsonDeserialize(using = DoubleDeSerializer.class)
	private Double invoiceAmount;

	@JsonAlias("InvoiceDate")
	@JsonDeserialize(using = DateDeSerializer.class)
	private Date invoiceDate;

	@JsonAlias("VendorCode")
	private String vendorCode;
	
	@JsonAlias("VendorName")
	private String vendorName;

	@JsonAlias("DueDate")
	@JsonDeserialize(using = DateDeSerializer.class)
	private Date dueDate;

	@JsonAlias("PaymentIdentifier")
	private String paymentIdentifier;
	
	@JsonAlias("ProcessingDate")
	@JsonDeserialize(using = DateDeSerializer.class)
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
