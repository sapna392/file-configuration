package core.com.file.management.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import core.com.file.management.model.VendorInvoiceStatus;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "COM_VENDOR_TXN_INVC")
@Getter @Setter
public class VendorTxnInvoiceEntity extends AbstractEntity{
	
	@Id
	@Column(name = "TXN_INVC_ID")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name = "FILE_ID")
	private String fileId;
	
	@Column(name = "IM_CODE")
	private String imCode;

	@Column(name = "INVC_NMBR")
	private String invoiceNumber;

	@Column(name = "INVC_AMNT")
	private String invoiceAmount;

	@Column(name = "INVC_DATE")
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date invoiceDate;

	@Column(name = "VNDR_CODE")
	private String vendorCode;
	
	@Column(name = "VNDR_NAME")
	private String vendorName;

	@Column(name = "DUE_DATE")
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date dueDate;

	@Column(name = "PYMT_IDFR")
	private String paymentIdentifier;
	
	@Column(name = "PRCSSNG_DATE")
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date processingDate;
	
	@Column(name = "INVC_STATUS")
	@Enumerated(EnumType.STRING)
	private VendorInvoiceStatus status;
	
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
