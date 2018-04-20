package uk.nhs.fhir.server_renderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public class RenderedArtefactsDirectory implements Supplier<Path> {
	private final Path outputDirectory;
	
	public RenderedArtefactsDirectory() {
    	try {
			outputDirectory = Files.createTempDirectory("fhir_renderer_tmp_");
		} catch (IOException e) {
			// critical error - fall over
			throw new IllegalStateException(e);
		}
	}
	
	public Path get() {
		return outputDirectory;
	}
}
