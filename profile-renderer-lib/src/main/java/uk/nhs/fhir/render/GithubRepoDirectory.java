package uk.nhs.fhir.render;

import java.nio.file.Path;

public class GithubRepoDirectory {
	private final Path location;
	private final String name;
	private final String branch;
	
	public GithubRepoDirectory(Path location, String name, String branch) {
		this.location = location;
		this.name = name;
		this.branch = branch;
	}
	
	public Path getLocation() {
		return location;
	}
	
	public String getName() {
		return name;
	}
	
	public String getBranch() {
		return branch;
	}
}
