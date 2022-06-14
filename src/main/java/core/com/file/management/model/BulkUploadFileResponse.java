package core.com.file.management.model;

import java.util.List;

import lombok.Data;

@Data
public class BulkUploadFileResponse {

	private String status;
	private int status_code;
	private String status_msg;
	private  ResponseMetadata metadata;
	private List<BulkUploadFileRest> data;
	
}
