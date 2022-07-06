package core.com.file.management.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import core.com.file.management.model.VendorInvoiceStatus;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "COM_ERR_FILE_DTLS")
@Getter @Setter
public class ErrorFileDetailsEntity extends AbstractEntity{

	@Id
	@Column(name = "ERR_FILE_ID")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name = "IM_CODE")
	private String imCode;
	
	@Column(name = "FILE_NAME")
	private String name;

	@Column(name = "FILE_STATUS")
	@Enumerated(EnumType.STRING)
	private VendorInvoiceStatus status;

	@Column(name = "FILE_TYPE")
	private String type;
	
	@Column(name = "FILE_GUID")
	private String guid;
	
	@Column(name = "FILE_HASH")
	private String hash;
	
	@Type(type="yes_no")
	@Column(name= "IS_ERROR_FILE")
	Boolean isErrorFile = Boolean.TRUE;
}
