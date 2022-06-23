package core.com.file.management.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ErrorFileConfigurationResponse {
	
	@ApiModelProperty(value = "Status")
	private String status;
	
	@ApiModelProperty(value = "Status code")
	private String status_code;
	
	@ApiModelProperty(value = "Status message")
	private String status_msg;
	
	@ApiModelProperty(value = "Error file configuration rest model")
	private ErrorFileConfigurationRest data;

}
