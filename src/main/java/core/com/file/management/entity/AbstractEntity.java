package core.com.file.management.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@MappedSuperclass
public class AbstractEntity {
	
	@Column(name = "CREATED"/* , nullable = false */)
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date created;
	
	@Column(name = "CREATED_BY"/* , nullable = false */)
	private String createdBy;
	
	@Column(name = "UPDATED"/* , nullable = false */)
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date updated;
	
	@Column(name = "UPDATED_BY"/* , nullable = false */)
	private String updatedBy;

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	
}
