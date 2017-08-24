package uk.nhs.fhir.data;

import java.util.Optional;

import com.google.common.base.Preconditions;

import uk.nhs.fhir.data.url.FhirURL;

public class ResourceInfo {
	private final String constraintName;
	private final Optional<String> description;
	private final Optional<FhirURL> descriptionLink;
	private String qualifier = "";
	private final boolean textualLink;
	
	private final ResourceInfoType type;

	public ResourceInfo(String constraintName, String description, ResourceInfoType type) {
		this(constraintName, Optional.of(description), Optional.empty(), type);
	}
	public ResourceInfo(String constraintName, FhirURL descriptionLink, ResourceInfoType type) {
		this(constraintName, Optional.empty(), Optional.of(descriptionLink), type);
	}
	public ResourceInfo(String constraintName, String description, FhirURL descriptionLink, ResourceInfoType type) {
		this(constraintName, Optional.of(description), Optional.of(descriptionLink), type);
	}

	public ResourceInfo(String constraintName, Optional<String> description, Optional<FhirURL> descriptionLink, ResourceInfoType type) {
		this(constraintName, description, descriptionLink, type, false);
	}
	
	public ResourceInfo(String constraintName, Optional<String> description, Optional<FhirURL> descriptionLink, ResourceInfoType type, boolean textualLink) {
		Preconditions.checkArgument(description.isPresent() || descriptionLink.isPresent(), "Constraint without description or link");
		
		if (!textualLink 
		  && descriptionLink.isPresent() 
		  && FhirURL.isLogicalUrl(descriptionLink.get().toLinkString())) {
			throw new IllegalStateException("Storing logical URL as link data");
		}
		
		this.constraintName = constraintName;
		this.description = description;
		this.descriptionLink = descriptionLink;
		this.type = type;
		this.textualLink = textualLink;
	}
	
	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}
	
	public String getName() {
		return constraintName;
	}
	
	public Optional<String> getDescription() {
		return description;
	}
	
	public Optional<FhirURL> getDescriptionLink() {
		return descriptionLink;
	}
	
	public String getQualifier() {
		return qualifier ;
	}
	
	public ResourceInfoType getType() {
		return type;
	}
	
	public boolean getTextualLink() {
		return textualLink;
	}
}