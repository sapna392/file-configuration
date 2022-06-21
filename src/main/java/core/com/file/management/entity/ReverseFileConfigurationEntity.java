/**
 * created by supro
 */
package core.com.file.management.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "COM_REVRSE_FILE_CONFG")
@Getter @Setter
public class ReverseFileConfigurationEntity extends AbstractEntity{
	
	public ReverseFileConfigurationEntity() {}
	
	public ReverseFileConfigurationEntity(String fileStructure, String fileDelimiter, String fileId, String referenceNo, 
			String creationTime, String reversalDate,String invoiceNumber, String invoiceAmount, String invoiceDate, 
			String vendorCode, String vendorName, String status, String statusDescription, String echequeNo,
			Date created, String createdBy) {
		super();
		this.fileStructure = fileStructure;
		this.fileDelimiter = fileDelimiter;
		this.fileId = fileId;
		this.referenceNo = referenceNo;
		this.creationTime = creationTime;
		this.invoiceNumber = invoiceNumber;
		this.invoiceAmount = invoiceAmount;
		this.invoiceDate = invoiceDate;
		this.reversalDate = reversalDate;
		this.vendorCode = vendorCode;
		this.vendorName = vendorName;
		this.status = status;
		this.statusDescription = statusDescription;
		this.echequeNo = echequeNo;
		this.setCreated(created);
		this.setCreatedBy(createdBy);
	}

	public ReverseFileConfigurationEntity(String fileStructure, String fileDelimiter, String fileId, String referenceNo, 
			String creationTime, String reversalDate,String invoiceNumber, String invoiceAmount, String invoiceDate, 
			String vendorCode, String vendorName, String status, String statusDescription, String echequeNo, 
			String additionalField1, String additionalField2, String additionalField3, String additionalField4, 
			String additionalField5, String additionalField6, String additionalField7, String additionalField8, 
			String additionalField9, String additionalField10) {
		super();
		this.fileStructure = fileStructure;
		this.fileDelimiter = fileDelimiter;
		this.fileId = fileId;
		this.referenceNo = referenceNo;
		this.creationTime = creationTime;
		this.invoiceNumber = invoiceNumber;
		this.invoiceAmount = invoiceAmount;
		this.invoiceDate = invoiceDate;
		this.reversalDate = reversalDate;
		this.vendorCode = vendorCode;
		this.vendorName = vendorName;
		this.status = status;
		this.statusDescription = statusDescription;
		this.echequeNo = echequeNo;
		this.additionalField1 = additionalField1;
		this.additionalField2 = additionalField2;
		this.additionalField3 = additionalField3;
		this.additionalField4 = additionalField4;
		this.additionalField5 = additionalField5;
		this.additionalField6 = additionalField6;
		this.additionalField7 = additionalField7;
		this.additionalField8 = additionalField8;
		this.additionalField9 = additionalField9;
		this.additionalField10 = additionalField10;
	}

	@Id
	@Column(name = "FILE_CONFIG_ID")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id = null;
	
	@Column(name = "IM_CODE", nullable = false)
	private String imCode;
	
	@Column(name = "FILE_STRUCT", nullable = false)
	private String fileStructure;

	@Column(name = "FILE_DLMTR")
	private String fileDelimiter;
	
	@Column(name = "FILE_ID")
	private String fileId;
	
	@Column(name = "REFERENCE_NO")
	private String referenceNo;
	
	@Column(name = "CREATION_TIME")
	private String creationTime;

	@JsonProperty("InvoiceNumber")
	@Column(name = "INVC_NMBR")
	private String invoiceNumber;

	@JsonProperty("InvoiceAmount")
	@Column(name = "INVC_AMNT")
	private String invoiceAmount;

	@JsonProperty("InvoiceDate")
	@Column(name = "INVC_DATE")
	private String invoiceDate;
	
	@Column(name = "REVERSAL_DATE")
	private String reversalDate;

	@JsonProperty("VendorCode")
	@Column(name = "VNDR_CODE")
	private String vendorCode;
	
	@JsonProperty("VendorName")
	@Column(name = "VNDR_NAME")
	private String vendorName;
	
	@JsonProperty("Status")
	@Column(name = "STATUS")
	private String status;

	@JsonProperty(value = "StatusDescription")
	@Column(name = "STATUS_DESC")
	private String statusDescription;
	
	@JsonProperty(value = "EchequeNo")
	@Column(name = "ECHEQUE_NO")
	private String echequeNo;
	
	@Column(name = "ADDITIONAL_FIELD_1")
	private String additionalField1 = null;
	
	@Column(name = "ADDITIONAL_FIELD_2")
	private String additionalField2 = null;
	
	@Column(name = "ADDITIONAL_FIELD_3")
	private String additionalField3 = null;
	
	@Column(name = "ADDITIONAL_FIELD_4")
	private String additionalField4 = null;
	
	@Column(name = "ADDITIONAL_FIELD_5")
	private String additionalField5 =null;
	
	@Column(name = "ADDITIONAL_FIELD_6")
	private String additionalField6 = null;
	
	@Column(name = "ADDITIONAL_FIELD_7")
	private String additionalField7 = null;
	
	@Column(name = "ADDITIONAL_FIELD_8")
	private String additionalField8 = null;
	
	@Column(name = "ADDITIONAL_FIELD_9")
	private String additionalField9 = null;
	
	@Column(name = "ADDITIONAL_FIELD_10")
	private String additionalField10 = null;
}
