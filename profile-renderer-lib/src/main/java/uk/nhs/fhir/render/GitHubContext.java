package uk.nhs.fhir.render;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.kohsuke.github.AbuseLimitHandler;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.RateLimitHandler;
import org.kohsuke.github.extras.OkHttpConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

public class GitHubContext {
	private static final Logger LOG = LoggerFactory.getLogger(RendererContext.class);
	
	private static final long HTTP_CACHE_MAX_SIZE = 10 * 1024 * 1024; // 10MB cache

	private Optional<GitHub> github = Optional.empty();
	private boolean triedGithubConnect = false;
	private boolean exceededRateLimit = false;
	private Map<String, GithubAccess> githubRepos = Maps.newHashMap();
	private Optional<Cache> httpCache = Optional.empty();
	
	private Optional<GithubRepoDirectory> currentGitDir = Optional.empty();
	private Optional<GHRepository> currentGitRepo = Optional.empty();
	
	public void setCacheDirectory(File httpCacheDirectory) {
		Preconditions.checkNotNull(httpCacheDirectory);
		Preconditions.checkArgument(httpCacheDirectory.isDirectory());
		
		this.httpCache = Optional.of(new Cache(httpCacheDirectory, HTTP_CACHE_MAX_SIZE));
	}
	
	private boolean githubAvailable() {
		return github.isPresent()
		  && !exceededRateLimit;
	}
	
	public void setGitRepos(List<GithubRepoDirectory> gitRepos) {
		Preconditions.checkNotNull(gitRepos);
		
		if (!this.github.isPresent()
		  && !triedGithubConnect) {
			connectToGithub();
		}
		
		if (githubAvailable()) {
			githubRepos.clear();
			for (GithubRepoDirectory dir : gitRepos) {
				if (githubAvailable()) {
					try {
						GithubAccess repo = new GithubAccess(github.get(), dir);
						githubRepos.put(repo.getDirectory().getName(), repo);
					} catch (IOException | GithubRateLimitOrAbuseException e) {
						LOG.warn("Exception getting repository " + dir.getName() + " using GitHub API");
						break;
					}
				}
			}
		} else {
			LOG.warn("Tried to set github repos but github connection was not available");
		}
	}
	
	private final RateLimitHandler githubRateLimitHandler = new RateLimitHandler() {
		@Override
		public void onError(IOException e, HttpURLConnection uc) throws IOException {
			exceededRateLimit = true;
			LOG.warn("Exceeded rate limiting usage of GitHub API. Not going to attempt to access it again.");
			throw new GithubRateLimitOrAbuseException();
		}
	};
	
	private final AbuseLimitHandler githubAbuseLimitHandler = new AbuseLimitHandler() {
		@Override
		public void onError(IOException e, HttpURLConnection uc) throws IOException {
			exceededRateLimit = true;
			LOG.warn("Triggered GitHub API abuse response. Not going to attempt to access it again.");
			throw new GithubRateLimitOrAbuseException();
		}
	};
	
	private void connectToGithub() {
		triedGithubConnect = true;
		
		GitHub connection = null;
		try {
			if (httpCache.isPresent()) {
				connection = GitHubBuilder.fromEnvironment()
				    .withConnector(new OkHttpConnector(new OkUrlFactory(new OkHttpClient().setCache(httpCache.get()))))
				    .withRateLimitHandler(githubRateLimitHandler)
				    .withAbuseLimitHandler(githubAbuseLimitHandler)
					.build();
			} else {
				connection = GitHubBuilder.fromEnvironment()
				    .withRateLimitHandler(githubRateLimitHandler)
				    .withAbuseLimitHandler(githubAbuseLimitHandler)
				    .build();
			}
		} catch (IOException e) {
			try {
				LOG.info("Unable to connect using Github credentials from the environment, fall back on anonymous access "
					+ "(likely to exceed permitted rate limit of 60 requests per hour)");
				
				connection = new GitHubBuilder()
				    .withRateLimitHandler(githubRateLimitHandler)
				    .withAbuseLimitHandler(githubAbuseLimitHandler)
				    .build();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		this.github = Optional.ofNullable(connection);
	}
	
	public Optional<List<GHCommit>> getGithubCommits(String filename) {
		return getGithubCommits(filename, 1);
	}

	public Optional<List<GHCommit>> getGithubCommits(String filename, int retries) {
		if (currentGitRepo.isPresent()
		  && githubAvailable()) {
			LOG.debug("Attempting to get git history for file: " + filename);
			try {
				return Optional.of(currentGitRepo.get().queryCommits().from(currentGitDir.get().getBranch()).path(filename).list().asList());
			} catch (GHException e) {
				if (retries > 0) {
					try {
						// A requests occasionally fail for reasons other than intentional limiting. Allow for 1 retry. 
						Thread.sleep(150);
						return getGithubCommits(filename, retries-1);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			} catch (GithubRateLimitOrAbuseException e) {}
		}
		
		return Optional.empty();
	}

	public Map<String, GithubAccess> getGithubRepos() {
		return githubRepos;
	}

	public void setCurrentGitDir(Optional<GithubRepoDirectory> currentGitDir) {
		this.currentGitDir = currentGitDir;
	}

	public void setCurrentGitRepo(Optional<GHRepository> currentGitRepo) {
		this.currentGitRepo = currentGitRepo;
	}
	
	public Optional<GithubRepoDirectory> getCurrentGitRepoDirectory() {
		return currentGitDir;
	}
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