package core.com.file.management.service;

import core.com.file.management.exception.FileConfigurationException;
import core.com.file.management.model.ErrorFileConfigurationRest;

public interface ErrorFileConfigurationService {

	public ErrorFileConfigurationRest saveFileConfiguration(ErrorFileConfigurationRest configurationRest)
			throws FileConfigurationException;

	public ErrorFileConfigurationRest viewFileConfiguration(String imCode);
}
