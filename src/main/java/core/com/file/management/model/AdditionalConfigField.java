package core.com.file.management.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AdditionalConfigField implements Serializable {

	private static final long serialVersionUID = 3181797792818359658L;

	@ApiModelProperty(name = "Field", value = "Field for the additional configuration") 
	private String configName;
	
	@ApiModelProperty(name = "Value", value = "Order/Position for the additional configuration") 
	private String configPos;
	
}
