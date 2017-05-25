package uk.nhs.fhir.makehtml.data;

import java.util.Optional;

public class ConstraintInfo {

	private final String key;
	private final String description;
	private final String severity;
	private final Optional<String> requirements;
	private final String xpath;

	public ConstraintInfo(String key, String description, String severity, Optional<String> requirements,
			String xpath) {
		this.key  = key;
		this.description = description;
		this.severity = severity;
		this.requirements = requirements;
		this.xpath = xpath;
	}

	public String getKey() {
		return key;
	}

	/**
	 * On the element, the field is 'human'
	 */
	public String getDescription() {
		return description;
	}
	
	public Optional<String> getRequirements() {
		return requirements;
	}

	public String getXPath() {
		return xpath;
	}

	public String getSeverity() {
		return severity;
	}

	@Override
	public int hashCode() {
		return key.hashCode()
			+ description.hashCode()
			+ requirements.hashCode()
			+ xpath.hashCode()
			+ severity.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (!(other instanceof ConstraintInfo)) {
			return false;
		}
		
		ConstraintInfo otherConstraintInfo = (ConstraintInfo)other;
		
		return key.equals(otherConstraintInfo.getKey())
			&& description.equals(otherConstraintInfo.getDescription())
			&& requirements.equals(otherConstraintInfo.getRequirements())
			&& xpath.equals(otherConstraintInfo.getXPath())
			&& severity.equals(otherConstraintInfo.getSeverity());
	}
}
