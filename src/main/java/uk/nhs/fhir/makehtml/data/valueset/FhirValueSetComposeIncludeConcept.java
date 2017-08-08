package uk.nhs.fhir.makehtml.data.valueset;

import java.util.Optional;

import com.google.common.base.Preconditions;

public class FhirValueSetComposeIncludeConcept {
	private final String code;
	private final Optional<String> description;
	
	public FhirValueSetComposeIncludeConcept(String code, String description) {
		Preconditions.checkNotNull(code);
		
		this.code = code;
		this.description = Optional.ofNullable(description);
	}

	public String getCode() {
		return code;
	}

	public Optional<String> getDescription() {
		return description;
	}
}
