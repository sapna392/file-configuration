package core.com.file.management.service;

import core.com.file.management.exception.FileConfigurationException;
import core.com.file.management.exception.NotFoundException;
import core.com.file.management.model.ReverseFileConfigurationRest;

public interface ReverseFileConfigurationService {

	public ReverseFileConfigurationRest saveFileConfiguration(ReverseFileConfigurationRest configurationRest)
			throws FileConfigurationException;

	public ReverseFileConfigurationRest viewFileConfiguration(String imCode) throws NotFoundException;
}
