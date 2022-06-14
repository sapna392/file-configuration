package core.com.file.management.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiParam;
import lombok.Data;

@ApiModel(value = "File configuration model", 
			description = "This class is used to store the order or position of the fields of uploaded invoice file")
@Data
public class FileConfigurationRest implements Serializable{

	private static final long serialVersionUID = -5081204324974166611L;
	
	@ApiParam(value = "User ID")
	private String userId;
	
	@ApiParam(value = "User type")
	private String userType;
	
	@ApiParam(value = "File structure")
	private String fileStructure;

	@ApiParam(value = "Delimiter")
	private String fileDelimiter;

	@ApiParam(value = "File configuration fields")
	private FileConfigurationFields configurationFields;

}
