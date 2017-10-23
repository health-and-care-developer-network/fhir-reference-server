package uk.nhs.fhir.makehtml;

import java.nio.file.Path;

import uk.nhs.fhir.data.wrap.WrappedResource;

public class DefaultRendererFileLocator implements RendererFileLocator {

	private final Path rawArtefactDirectory;
	private final Path tempOutputDirectory;
	private final Path finalOutputDirectory;
	
	public DefaultRendererFileLocator(Path rawArtefactDirectory, Path tempOutputDirectory, Path finalOutputDirectory) {
		this.rawArtefactDirectory = rawArtefactDirectory;
		this.tempOutputDirectory = tempOutputDirectory;
		this.finalOutputDirectory = finalOutputDirectory;
	}

	@Override
	public Path getRawArtefactDirectory() {
		return rawArtefactDirectory;
	}

	@Override
	public Path getRenderingTempOutputDirectory() {
		return tempOutputDirectory;
	}

	@Override
	public Path getRenderingTempOutputDirectory(WrappedResource<?> resource) {
		return tempOutputDirectory.resolve(resource.getImplicitFhirVersion().toString()).resolve(resource.getResourceType().getDisplayName());
	}

	@Override
	public Path getRenderingFinalOutputDirectory() {
		return finalOutputDirectory;
	}

}
