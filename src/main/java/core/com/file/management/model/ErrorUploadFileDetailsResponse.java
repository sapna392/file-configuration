package core.com.file.management.model;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ErrorUploadFileDetailsResponse implements Serializable{

	private static final long serialVersionUID = 6626097369473880545L;
	
	@ApiModelProperty(value = "Status")
	private String status;
	
	@ApiModelProperty(value = "Status code")
	private String status_code;
	
	@ApiModelProperty(value = "Status message")
	private String status_msg;
	
	@ApiModelProperty(value = "Medata regarding the list of files")
	private  ResponseMetadata metadata;
	
	@ApiModelProperty(value = "Reverse file details")
	private List<ErrorUploadFileDetailsRest> data;

}
