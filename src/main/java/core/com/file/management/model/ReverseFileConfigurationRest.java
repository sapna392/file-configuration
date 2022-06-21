package core.com.file.management.model;

import java.io.Serializable;

import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
public class ReverseFileConfigurationRest implements Serializable{

	private static final long serialVersionUID = -7740234922183060473L;
	
	@ApiParam(value = "IM Code")
	private String imCode;
	
	@ApiParam(value = "File structure")
	private String fileStructure;

	@ApiParam(value = "Delimiter")
	private String fileDelimiter;
	
	@ApiParam(value = "Reverse File configuration fields")
	private ReverseFileConfigurationFields configurationFields;

}
