package uk.nhs.fhir.server_renderer;

import java.util.Optional;
import java.util.Set;

public class ServerRendererArgs {
	private final boolean largeText;
	private final Optional<Set<String>> localDomains;
	private final Optional<Set<String>> allowedMissingExtensionPrefixes;
	
	public ServerRendererArgs(boolean largeText, Optional<Set<String>> localDomains, Optional<Set<String>> allowedMissingExtensionPrefixes) {
		this.largeText = largeText;
		this.localDomains = localDomains;
		this.allowedMissingExtensionPrefixes = allowedMissingExtensionPrefixes;
	}
	
	public boolean getLargeText() {
		return largeText;
	}
	
	public Optional<Set<String>> getLocalDomains() {
		return localDomains;
	}
	
	public Optional<Set<String>> getAllowedMissingExtensionPrefixes() {
		return allowedMissingExtensionPrefixes;
	}
}
