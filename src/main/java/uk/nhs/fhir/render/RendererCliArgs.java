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
    private final Optional<String> repositoryName;
    private final Optional<String> repositoryBranch;
    private final Optional<String> httpCacheDirectory;
	
	public RendererCliArgs(Path inputDir, Path outputDir, Optional<String> newBaseUrl,
				Optional<Set<String>> allowedMissingExtensionPrefixes,
				Optional<String> repositoryName,
				Optional<String> repositoryBranch,
				Optional<String> httpCacheDirectory,
				Optional<Set<String>> localDomains) {
		this.inputDir = inputDir;
		this.outputDir = outputDir;
		this.newBaseUrl = newBaseUrl;
		this.allowedMissingExtensionPrefixes = allowedMissingExtensionPrefixes;
		this.localDomains = localDomains;
		this.repositoryName = repositoryName;
		this.repositoryBranch = repositoryBranch;
		this.httpCacheDirectory = httpCacheDirectory;
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

	public Optional<String> getRepositoryName() {
		return repositoryName;
	}

	public Optional<String> getRepositoryBranch() {
		return repositoryBranch;
	}

	public Optional<String> getHttpCacheDirectory() {
		return httpCacheDirectory;
	}
}
