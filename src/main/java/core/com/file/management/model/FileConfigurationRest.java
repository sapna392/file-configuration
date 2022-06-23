package core.com.file.management.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "File configuration model", 
			description = "This class is used to store the order or position of the fields of uploaded invoice file")
@Data
public class FileConfigurationRest implements Serializable{

	private static final long serialVersionUID = -5081204324974166611L;
	
	@ApiModelProperty(value = "IM Code")
	private String imCode;
	
	@ApiModelProperty(value = "File structure")
	private String fileStructure;

	@ApiModelProperty(value = "Delimiter")
	private String fileDelimiter;

	@ApiModelProperty(value = "File configuration fields")
	private FileConfigurationFields configurationFields;

}
