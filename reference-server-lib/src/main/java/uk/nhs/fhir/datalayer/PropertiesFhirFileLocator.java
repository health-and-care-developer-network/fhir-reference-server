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
		return rootDirFromProperties.resolve(resourceFolderPrefixFromProperties).resolve(fhirVersion.toString());
	}
	
	public Path getDestinationPathForResourceType(ResourceType type, FhirVersion version) {
		return getSourcePathForResourceType(type, version).resolve("versioned");
	}
}
