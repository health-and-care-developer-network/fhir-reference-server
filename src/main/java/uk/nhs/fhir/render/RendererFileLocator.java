package uk.nhs.fhir.render;

import java.nio.file.Path;

import uk.nhs.fhir.data.wrap.WrappedResource;

public interface RendererFileLocator {

	Path getRawArtefactDirectory();
	Path getRenderingTempOutputDirectory();
	Path getRenderingTempOutputDirectory(WrappedResource<?> resource);
	Path getRenderingFinalOutputDirectory();
	
}
