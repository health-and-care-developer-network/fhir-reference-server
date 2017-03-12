package uk.nhs.fhir.makehtml;

import java.util.List;

import com.google.common.collect.Lists;

public class ResourceFlag {
	private final String flagName;
	private final String description;
	private final boolean descriptionIsLink;
	private final List<String> extraTags= Lists.newArrayList();
	
	public ResourceFlag(String flagName, String description, boolean descriptionIsLink) {
		this.flagName = flagName;
		this.description = description;
		this.descriptionIsLink = descriptionIsLink;
	}
	
	public void addExtraTag(String tag) {
		extraTags.add(tag);
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
	
	public List<String> getExtraTags() {
		return extraTags;
	}
}