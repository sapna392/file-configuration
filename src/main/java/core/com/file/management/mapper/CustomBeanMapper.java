package core.com.file.management.mapper;

import org.dozer.DozerBeanMapper;
import org.dozer.loader.api.BeanMappingBuilder;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomBeanMapper extends DozerBeanMapper{

	BeanMappingBuilder beanMappingBuilder = new BeanMappingBuilder() {
	      @Override
	      protected void configure() {
	    	  //insert custom mappings here
	      }
	};
	
	
}
