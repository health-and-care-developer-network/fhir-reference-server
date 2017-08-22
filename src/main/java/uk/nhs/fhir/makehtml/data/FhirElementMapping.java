package uk.nhs.fhir.makehtml.data;

import java.util.Optional;

import com.google.common.base.Preconditions;

public class FhirElementMapping {
	private final String identity;
	private final String map;
	private final Optional<String> language;
	
	public FhirElementMapping(String identity, String map, Optional<String> language) {
		Preconditions.checkNotNull(identity);
		Preconditions.checkNotNull(map);
		Preconditions.checkNotNull(language);
		
		this.identity = identity;
		this.map = map;
		this.language = language;
	}
	
	public String getIdentity() {
		return identity;
	}
	public String getMap() {
		return map;
	}
	public Optional<String> getLanguage() {
		return language;
	}
	
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		
		if (!(other instanceof FhirElementMapping)) {
			return false;
		}
		
		FhirElementMapping otherFhirElementMapping = (FhirElementMapping)other;
		
		return identity.equals(otherFhirElementMapping.getIdentity())
		  && map.equals(otherFhirElementMapping.getMap())
		  && language.equals(otherFhirElementMapping.getLanguage());
	}
	
	public int hashCode() {
		return identity.hashCode() * map.hashCode() + language.hashCode();
	}
}
