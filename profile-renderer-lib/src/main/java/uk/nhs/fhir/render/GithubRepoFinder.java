package uk.nhs.fhir.render;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

/*
 * Collects directories in a repo which contain a .git folder which in turn contains a config and HEAD
 * from which a repo URL and branch can be identified, and caches these data on the assumption that they
 * are unlikely to change during rendering.
 */
public class GithubRepoFinder {

	private final Path root;
	
	public GithubRepoFinder(Path root) {
		this.root = root;
	}

	public List<GithubRepoDirectory> find() {
		if (!root.toFile().exists()) {
			throw new IllegalStateException("Root file doesn't exist: " + root.toString());
		}
		
		if (!root.toFile().isDirectory()) {
			throw new IllegalStateException("Root file is not a directory: " + root.toString());
		}
		
		List<File> directoriesWithGitFolders = Lists.newArrayList();
		findDirectoriesRecursive(root, directoriesWithGitFolders);
		
		List<GithubRepoDirectory> foundRepos = Lists.newArrayList();
		for (File dir : directoriesWithGitFolders) {
			String fullRepoUrl = null;
			File configFile = dir.toPath().resolve(".git").resolve("config").toFile();
			if (configFile.exists()) {
				try {
					boolean foundOrigin = false;
					for (String line : FileUtils.readLines(configFile, "UTF-8")) {
						String trimmed = line.trim();
						if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
							String[] section = trimmed.substring(1, trimmed.length()-1).split("\\s+");
							if (section.length >= 2
							  && section[0].equals("remote")
							  && section[1].equals("\"origin\"")) {
								foundOrigin = true;
							} else if (foundOrigin) {
								// we've finished the origin section and haven't found the repo url. No point continuing.
								break;
							}
						} else if (foundOrigin) {
							// we are in an entry within the [remote "origin"] section
							String[] section = trimmed.split("\\s+");
							if (section.length >= 3
							  && section[0].equals("url")
							  && section[1].equals("=")) {
								fullRepoUrl = section[2];
								// assume there is a single url in the "origin" remote
								break;
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (fullRepoUrl == null) {
				continue;
			}
			
			String branch = null;
			File headFile = dir.toPath().resolve(".git").resolve("HEAD").toFile();
			if (headFile.exists()) {
				try {
					String trimmed = FileUtils.readLines(headFile, "UTF-8").get(0).trim();
					if (trimmed.startsWith("ref:")) {
						branch = trimmed.substring("ref:".length()).trim();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (branch == null) {
				continue;
			}
			
			if (fullRepoUrl.startsWith("https://github.com/") && fullRepoUrl.endsWith(".git")) {
				String repoName = fullRepoUrl.substring("https://github.com/".length(), fullRepoUrl.length()-".git".length());
				foundRepos.add(new GithubRepoDirectory(dir.toPath().toAbsolutePath(), repoName, branch));
			}
		}
		
		return foundRepos;
	}
	
	private void findDirectoriesRecursive(Path p, List<File> directoriesWithGitFolders) {
		File f = p.toFile();
		if (f.isDirectory()) {
			if (p.resolve(".git").toFile().exists()
			  && p.resolve(".git").toFile().isDirectory()) {
				directoriesWithGitFolders.add(f);
			}
			
			for (File child : f.listFiles()) {
				findDirectoriesRecursive(child.toPath(), directoriesWithGitFolders);
			}
		}
	}

}
