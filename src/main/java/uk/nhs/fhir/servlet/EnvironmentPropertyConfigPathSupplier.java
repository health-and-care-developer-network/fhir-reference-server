package uk.nhs.fhir.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class EnvironmentPropertyConfigPathSupplier implements ConfigPathSupplier {
	private static final Logger LOG = LoggerFactory.getLogger(EnvironmentPropertyConfigPathSupplier.class);
	
	private static final String DEFAULT_PROPERTY_FILE = "fhirserver.config.properties";
	
	@Override
	public String get() {
		String envConfigFile = System.getenv("CONFIG_FILE");
		
		if (!Strings.isNullOrEmpty(envConfigFile)) {
			LOG.info("Using custom configuration from: " + envConfigFile);
			return envConfigFile;
		} else {
			return DEFAULT_PROPERTY_FILE;
		}
	}

}
