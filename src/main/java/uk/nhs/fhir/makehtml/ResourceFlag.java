package uk.nhs.fhir.makehtml;

public class ResourceFlag {
	private final String flagName;
	private final String description;
	private final boolean descriptionIsLink;
	
	public ResourceFlag(String flagName, String description, boolean descriptionIsLink) {
		this.flagName = flagName;
		this.description = description;
		this.descriptionIsLink = descriptionIsLink;
	}
	
	public String getName() {
		return flagName;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean descriptionIsLink() {
		return descriptionIsLink;
	}
}
