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
	
	@JsonAlias("InvoiceNumber")
	private String invoiceNumber;

	@JsonAlias("InvoiceAmount")
	private Double invoiceAmount;

	@JsonAlias("InvoiceDate")
	@JsonDeserialize(using = DateDeSerializer.class)
	@JsonFormat(shape = JsonFormat.Shape.ANY, pattern="dd/MM/yyyy")
	private Date invoiceDate;

	@JsonAlias("VendorCode")
	private String vendorCode;
	
	@JsonAlias("VendorName")
	private String vendorName;

	@JsonAlias("DueDate")
	@JsonDeserialize(using = DateDeSerializer.class)
	@JsonFormat(shape = JsonFormat.Shape.ANY, pattern="dd/MM/yyyy")
	private Date dueDate;

	@JsonAlias("PaymentIdentifier")
	private String paymentIdentifier;
	
	@JsonAlias("ProcessingDate")
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
