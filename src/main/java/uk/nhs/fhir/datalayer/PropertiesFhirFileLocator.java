package uk.nhs.fhir.datalayer;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.util.AbstractFhirFileLocator;
import uk.nhs.fhir.util.FhirServerProperties;
import uk.nhs.fhir.util.FhirVersion;

/**
 * Used by the FHIR server to configure importing files from the rendered documents folder, into the FileCache data store.
 * Generates versioned files in directory called 'versioned' next to the source files
 */
public class PropertiesFhirFileLocator extends AbstractFhirFileLocator {

	private static final Logger LOG = LoggerFactory.getLogger(PropertiesFhirFileLocator.class);
	
	private static final String PROP_ROOT_PATH = "defaultResourceRootPath";
	private static final String DSTU2_DIRECTORY = "NHSDigital";
	private static final String STU3_DIRECTORY = "NHSDigitalSTU3";
	
	private final Path rootDirFromProperties;
	public PropertiesFhirFileLocator() {
		String property = FhirServerProperties.getProperty(PROP_ROOT_PATH);
		LOG.info("Root path: " + property);
		rootDirFromProperties = Paths.get(property);
	}
	
	@Override
	public Path getSourceRoot(FhirVersion fhirVersion) {
		switch(fhirVersion) {
		case DSTU2:
			return rootDirFromProperties.resolve(DSTU2_DIRECTORY);
		case STU3:
			return rootDirFromProperties.resolve(STU3_DIRECTORY);
		default:
			throw new IllegalStateException("No default file path for FHIR version " + fhirVersion.toString());
		}
	}
	
	public Path getDestinationPathForResourceType(ResourceType type, FhirVersion version) {
		return getSourcePathForResourceType(type, version).resolve("versioned");
	}
}
