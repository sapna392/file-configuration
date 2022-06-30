package core.com.file.management.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import core.com.file.management.mapper.DateDeSerializer;
import lombok.Data;

@Data
public class VendorTxnInvoiceRest {

	private String imCode;
	
	private String fileId;
	
	@JsonAlias("Invoice Number")
	private String invoiceNumber;

	@JsonAlias("Invoice Amount")
	private Double invoiceAmount;

	@JsonAlias("Invoice Date")
	@JsonDeserialize(using = DateDeSerializer.class)
	@JsonFormat(shape = JsonFormat.Shape.ANY, pattern="dd/MM/yyyy")
	private Date invoiceDate;

	@JsonAlias("Vendor Code")
	private String vendorCode;
	
	@JsonAlias("Vendor Name")
	private String vendorName;

	@JsonAlias("Due Date")
	@JsonDeserialize(using = DateDeSerializer.class)
	@JsonFormat(shape = JsonFormat.Shape.ANY, pattern="dd/MM/yyyy")
	private Date dueDate;

	@JsonAlias("Payment Identifier")
	private String paymentIdentifier;
	
	@JsonAlias("Processing Date")
	@JsonDeserialize(using = DateDeSerializer.class)
	@JsonFormat(shape = JsonFormat.Shape.ANY, pattern="dd/MM/yyyy")
	private Date processingDate;	
	
	private VendorInvoiceStatus status;
	
	List<AdditionalConfigField> additionalConfigFieldList;
	
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
