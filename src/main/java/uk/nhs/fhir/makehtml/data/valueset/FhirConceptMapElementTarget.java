package uk.nhs.fhir.makehtml.data.valueset;

import java.util.Optional;

public class FhirConceptMapElementTarget {

	private final String code;
	private final String equivalence;
	private final Optional<String> comments;
	
	public FhirConceptMapElementTarget(String code, String equivalence, Optional<String> comments) {
		this.code = code;
		this.equivalence = equivalence;
		this.comments = comments;
	}

	public String getCode() {
		return code;
	}

	public String getEquivalence() {
		return equivalence;
	}
	
	public Optional<String> getComments() {
		return comments;
	}

}
