package core.com.file.management.service;

import core.com.file.management.exception.FileConfigurationException;
import core.com.file.management.model.FileConfigurationRest;

public interface FileConfigurationService {

	public FileConfigurationRest saveFileConfiguration(FileConfigurationRest configurationRest) throws FileConfigurationException;
	
	public FileConfigurationRest viewFileConfiguration(String userId, String userType) throws FileConfigurationException;

}
