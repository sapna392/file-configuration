package core.com.file.management.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "FILE_UPLOAD_DETAILS")
@Getter @Setter
public class BulkUploadFileEntity extends AbstractEntity {

	@Id
	@Column(name = "FILE_ID")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private String id;
	
	@Column(name = "IM_CODE")
	private String imCode;
	
	@Column(name = "FILE_NAME")
	private String name;

	@Column(name = "FILE_STATUS")
	private String status;

	@Column(name = "FILE_TYPE")
	private String type;
	
	@Column(name = "FILE_GUID")
	private String guid;
	
	@Column(name = "FILE_HASH")
	private String hash;
	
	@Column(name = "INVOICE_COUNT")
	private Integer invoiceCount;
	
	@Column(name = "TOTAL_AMOUNT")
	private Double totalAmount;

}
