package uk.nhs.fhir.render;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public class RendererCliArgs {
	private final Path inputDir;
	private final Path outputDir;
	private final Optional<String> newBaseUrl;
	private final Optional<Set<String>> allowedMissingExtensionPrefixes;
	private final Optional<Set<String>> localDomains;
	private final boolean copyOnError;
	
	public RendererCliArgs(
			Path inputDir, 
			Path outputDir, 
			Optional<String> newBaseUrl, 
			Optional<Set<String>> allowedMissingExtensionPrefixes, 
			Optional<Set<String>> localDomains,
			boolean copyOnError) {
		this.inputDir = inputDir;
		this.outputDir = outputDir;
		this.newBaseUrl = newBaseUrl;
		this.allowedMissingExtensionPrefixes = allowedMissingExtensionPrefixes;
		this.localDomains = localDomains;
		this.copyOnError = copyOnError;
	}

	public Path getInputDir() {
		return inputDir;
	}

	public Path getOutputDir() {
		return outputDir;
	}

	public Optional<String> getNewBaseUrl() {
		return newBaseUrl;
	}
	
	public Optional<Set<String>> getAllowedMissingExtensionPrefixes() {
		return allowedMissingExtensionPrefixes;
	}
	
	public Optional<Set<String>> getLocalDomains() {
		return localDomains;
	}
	
	public boolean getCopyOnError() {
		return copyOnError;
	}
}
