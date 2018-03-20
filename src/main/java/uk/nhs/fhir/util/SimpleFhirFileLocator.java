package uk.nhs.fhir.util;

import java.nio.file.Path;

import uk.nhs.fhir.data.metadata.ResourceType;

public class SimpleFhirFileLocator extends AbstractFhirFileLocator {

	private final Path sourceRoot;
	private final Path destinationRoot;
	
	public SimpleFhirFileLocator(Path sourceRoot, Path destinationRoot) {
		this.sourceRoot = sourceRoot;
		this.destinationRoot = destinationRoot; 
	}
	
	@Override
	public Path getSourceRoot(FhirVersion fhirVersion) {
		return sourceRoot.resolve(fhirVersion.toString());
	}

	@Override
	public Path getDestinationPathForResourceType(ResourceType type, FhirVersion version) {
		return destinationRoot.resolve(version.toString()).resolve(type.getFolderName());
	}
	
	public Path getSourceRoot() {
		return sourceRoot;
	}
	
	public Path getDestinationRoot() {
		return destinationRoot;
	}

}
