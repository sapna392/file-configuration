package core.com.file.management.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "Error File configuration model", 
	description = "This class is used to store the order or position of the fields of the reverse and error file")
@Data
public class ErrorFileConfigurationRest implements Serializable{

	private static final long serialVersionUID = -7740234922183060473L;
	
	@ApiModelProperty(value = "IM Code")
	private String imCode;
	
	@ApiModelProperty(value = "File structure")
	private String fileStructure;

	@ApiModelProperty(value = "Delimiter")
	private String fileDelimiter;
	
	@ApiModelProperty(value = "Error File configuration fields")
	private ErrorFileConfigurationFields configurationFields;

}
