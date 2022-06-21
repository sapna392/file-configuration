package core.com.file.management.model;

import lombok.Data;

@Data
public class ReverseFileConfigurationResponse {
	
	private String status;
	private String status_code;
	private String status_msg;
	private ReverseFileConfigurationRest data;

}
