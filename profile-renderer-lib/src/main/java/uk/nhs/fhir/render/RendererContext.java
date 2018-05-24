package uk.nhs.fhir.render;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.OkHttpConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.util.FhirFileRegistry;

public class RendererContext {
	private static final Logger LOG = LoggerFactory.getLogger(RendererContext.class);
	
	private static final ThreadLocal<RendererContext> theRendererContext = ThreadLocal.withInitial(RendererContext::new);
	
	private static final long HTTP_CACHE_MAX_SIZE = 10 * 1024 * 1024; // 10MB cache
	
	public static RendererContext forThread() {
		return theRendererContext.get();
	}
	
	private Set<String> permittedMissingExtensionPrefixes = Sets.newHashSet();
	private FhirFileRegistry fhirFileRegistry = new FhirFileRegistry();
	private File currentSource = null;
	private Optional<WrappedResource<?>> currentParsedResource = null;
	private Optional<GithubRepoDirectory> currentGitDir = Optional.empty();
	private Optional<GHRepository> currentGitRepo = Optional.empty();

	private Optional<GitHub> github = Optional.empty();
	private boolean triedGithubConnect = false;
	private Map<String, GithubAccess> githubRepos = Maps.newHashMap();
	private Optional<Cache> httpCache = Optional.empty();
	
	// TODO migrate local domains to here from FhirURL. Will require passing into FullFhirURL.toLinkString() though.
	// private DomainTrimmer localDomains = DomainTrimmer.nhsDomains();
	
	public RendererContext() {
		this(new FhirFileRegistry());
	}
	
	public RendererContext(FhirFileRegistry fhirFileRegistry) {
		this.fhirFileRegistry = fhirFileRegistry;
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
	
	public void setCacheDirectory(File httpCacheDirectory) {
		Preconditions.checkNotNull(httpCacheDirectory);
		Preconditions.checkArgument(httpCacheDirectory.isDirectory());
		
		this.httpCache = Optional.of(new Cache(httpCacheDirectory, HTTP_CACHE_MAX_SIZE));
	}
	
	public void setGitRepos(List<GithubRepoDirectory> gitRepos) {
		Preconditions.checkNotNull(gitRepos);
		
		if (!this.github.isPresent()
		  && !triedGithubConnect) {
			connectToGithub();
		}
		
		if (github.isPresent()) {
			for (GithubRepoDirectory dir : gitRepos) {
				try {
					GithubAccess repo = new GithubAccess(github.get(), dir);
					githubRepos.put(repo.getDirectory().getName(), repo);
				} catch (IOException e) {
					LOG.warn("Exception getting repository " + dir.getName() + " using GitHub API");
				}
			}
		} else {
			LOG.warn("Tried to set github repos but github connection was not available");
		}
	}
	
	private void connectToGithub() {
		triedGithubConnect = true;
		
		GitHub connection = null;
		try {
			if (httpCache.isPresent()) {
				connection = GitHubBuilder.fromEnvironment()
				    .withConnector(new OkHttpConnector(new OkUrlFactory(new OkHttpClient().setCache(httpCache.get()))))
					.build();
			} else {
				connection = GitHubBuilder.fromEnvironment().build();
			}
		} catch (IOException e) {
			try {
				LOG.info("Unable to connect using Github credentials from the environment, fall back on anonymous access");
				connection = GitHub.connectAnonymously();
				//this.repo = github.getRepository(this.gitRepo.getUrl());
			} catch (Exception ex) {
				ex.printStackTrace();
				//this.repo = null;
			}
		}
		
		this.github = Optional.ofNullable(connection);
	}

	public void setCurrentSource(File newSource) {
		currentSource = newSource;
		if (newSource == null) {
			this.currentGitDir = Optional.empty();
		} else {
			Optional<Entry<String, GithubAccess>> repo = 
				githubRepos
					.entrySet()
					.stream()
					.filter(entry -> currentSource.toPath().toAbsolutePath().startsWith(entry.getValue().getDirectory().getLocation()))
					.sorted(Comparator.comparing(filepath -> filepath.toString().length()))
					.findFirst();
			
			this.currentGitDir = repo.map(entry -> entry.getValue().getDirectory());
			this.currentGitRepo = repo.map(entry -> entry.getValue().getRepo());
		}
	}
	
	public Optional<GithubRepoDirectory> getCurrentGitRepoDirectory() {
		return currentGitDir;
	}
	
	public Optional<GHRepository> getCurrentGitRepo() {
		return currentGitRepo;
	}

	public Optional<WrappedResource<?>> getCurrentParsedResource() {
		return currentParsedResource;
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

	/*public DomainTrimmer getLocalDomains() {
		return localDomains;
	}*/
}

class GithubAccess {
	private final GithubRepoDirectory directory;
	private final GHRepository repo;
	
	public GithubAccess(GitHub github, GithubRepoDirectory directory) throws IOException {
		Preconditions.checkNotNull(github);
		
		this.directory = directory;
		this.repo = github.getRepository(directory.getName());
	}
	
	public GithubRepoDirectory getDirectory() {
		return directory;
	}
	
	public GHRepository getRepo() {
		return repo;
	}
}
