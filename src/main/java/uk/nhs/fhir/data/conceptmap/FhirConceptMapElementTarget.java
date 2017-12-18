package uk.nhs.fhir.data.conceptmap;

import java.util.Optional;

import com.google.common.base.Preconditions;

public class FhirConceptMapElementTarget {

	private final String code;
	private final String equivalence;
	private final Optional<String> comments;
	
	public FhirConceptMapElementTarget(String code, String equivalence, Optional<String> comments) {
		Preconditions.checkNotNull(code);
		Preconditions.checkNotNull(equivalence);
		
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
