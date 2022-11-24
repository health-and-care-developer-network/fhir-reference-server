package uk.nhs.fhir.render;

import java.io.File;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Sets;

import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.util.FhirFileRegistry;

public class RendererContext {
	
	private static final ThreadLocal<RendererContext> theRendererContext = ThreadLocal.withInitial(RendererContext::new);
	
	public static RendererContext forThread() {
		return theRendererContext.get();
	}
	
	private Set<String> permittedMissingExtensionPrefixes = Sets.newHashSet();
	private FhirFileRegistry fhirFileRegistry = new FhirFileRegistry();
	private File currentSource = null;
	private Optional<WrappedResource<?>> currentParsedResource = null;

	private final GitHubContext githubData;
	
	// TODO migrate local domains to here from FhirURL. Will require passing into FullFhirURL.toLinkString() though.
	// private DomainTrimmer localDomains = DomainTrimmer.nhsDomains();
	
	public RendererContext() {
		this(new FhirFileRegistry());
	}
	
	public RendererContext(FhirFileRegistry fhirFileRegistry) {
		this.fhirFileRegistry = fhirFileRegistry;
		this.githubData = new GitHubContext();
	}
	
	public FhirFileRegistry getFhirFileRegistry() {
		return fhirFileRegistry;
	}
	
	public void setFhirFileRegistry(FhirFileRegistry fhirFileRegistry) {
		this.fhirFileRegistry = fhirFileRegistry;
	}

	public File getCurrentSource() {
		return currentSource;
	}
	
	public GitHubContext github() {
		return this.githubData;
	}

	public void setCurrentSource(File newSource) {
		currentSource = newSource;
		
		if (newSource == null) {
			this.github().setCurrentGitDir(Optional.empty());
		} else {
			Optional<Entry<String, GithubAccess>> repo =
				github().getGithubRepos()
					.entrySet()
					.stream()
					.filter(entry -> currentSource.toPath().toAbsolutePath().startsWith(entry.getValue().getDirectory().getLocation()))
					.sorted(Comparator.comparing(filepath -> filepath.toString().length()))
					.findFirst();
			
			this.github().setCurrentGitDir(repo.map(entry -> entry.getValue().getDirectory()));
			this.github().setCurrentGitRepo(repo.map(entry -> entry.getValue().getRepo()));
		}
	}

	public Optional<WrappedResource<?>> getCurrentParsedResource() {
		return WrappedResource.getFullWrappedResourceIfSkeleton(currentParsedResource);
	}

	public void setCurrentParsedResource(Optional<WrappedResource<?>> newParsedResource) {
		currentParsedResource = newParsedResource;
	}

	public void clearCurrent() {
		setCurrentParsedResource(Optional.empty());
		setCurrentSource(null);
	}
	
	public Set<String> getPermittedMissingExtensionPrefixes() {
		return permittedMissingExtensionPrefixes;
	}
	
	public void setPermittedMissingExtensionPrefixes(Set<String> permittedMissingExtensionPrefixes) {
		this.permittedMissingExtensionPrefixes = permittedMissingExtensionPrefixes;
	}
}
