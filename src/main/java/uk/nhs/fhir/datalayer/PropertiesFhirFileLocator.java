package uk.nhs.fhir.datalayer;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.servlet.SharedServletContext;
import uk.nhs.fhir.util.AbstractFhirFileLocator;
import uk.nhs.fhir.util.FhirVersion;

/**
 * Used by the FHIR server to configure importing files from the rendered documents folder, into the FileCache data store.
 * Generates versioned files in directory called 'versioned' next to the source files
 */
public class PropertiesFhirFileLocator extends AbstractFhirFileLocator {

	private static final Logger LOG = LoggerFactory.getLogger(PropertiesFhirFileLocator.class);
	
	private static final String DSTU2_DIRECTORY_SUFFIX = "";
	private static final String STU3_DIRECTORY_SUFFIX = "STU3";
	
	private final Path rootDirFromProperties;
	private final String resourceFolderPrefixFromProperties;
	public PropertiesFhirFileLocator() {
		rootDirFromProperties = Paths.get(SharedServletContext.getProperties().getResourceRootPath());
		resourceFolderPrefixFromProperties = SharedServletContext.getProperties().getResourceFolderPrefix();

		LOG.debug("Root path: " + rootDirFromProperties.toString());
		LOG.debug("Folder prefix: " + resourceFolderPrefixFromProperties);
	}
	
	@Override
	public Path getSourceRoot(FhirVersion fhirVersion) {
		switch(fhirVersion) {
		case DSTU2:
			return rootDirFromProperties.resolve(resourceFolderPrefixFromProperties + DSTU2_DIRECTORY_SUFFIX);
		case STU3:
			return rootDirFromProperties.resolve(resourceFolderPrefixFromProperties + STU3_DIRECTORY_SUFFIX);
		default:
			throw new IllegalStateException("No default file path for FHIR version " + fhirVersion.toString());
		}
	}
	
	public Path getDestinationPathForResourceType(ResourceType type, FhirVersion version) {
		return getSourcePathForResourceType(type, version).resolve("versioned");
	}
}
