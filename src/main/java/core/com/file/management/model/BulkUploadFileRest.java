package core.com.file.management.model;

import java.util.Date;

import lombok.Data;

@Data
public class BulkUploadFileRest {
	
	private String id;
	private String imCode;
	private String name;
	private String status;
	private String type;
	private String guid;
	private Integer invoiceCount;
	private Double totalAmount;
	private Date created;
	private Date updated;
	
}
