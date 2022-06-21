package core.com.file.management.model;

import java.io.Serializable;

import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
public class AdditionalConfigField implements Serializable {

	private static final long serialVersionUID = 3181797792818359658L;

	@ApiParam(name = "Field", value = "Field for the additional configuration") 
	private String configName;
	
	@ApiParam(name = "Value", value = "Order/Position for the additional configuration") 
	private String configPos;
	
}
