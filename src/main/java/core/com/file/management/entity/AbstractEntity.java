package core.com.file.management.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@MappedSuperclass
public class AbstractEntity {
	
	@Column(name = "CREATED" , nullable = false )
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date created = new Date();
	
	@Column(name = "CREATED_BY" , nullable = false )
	private String createdBy;
	
	@Column(name = "UPDATED" , nullable = false )
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date updated = new Date();
	
	@Column(name = "UPDATED_BY" , nullable = false )
	private String updatedBy;

}
