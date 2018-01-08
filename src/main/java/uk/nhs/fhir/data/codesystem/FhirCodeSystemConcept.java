package uk.nhs.fhir.data.codesystem;

import java.util.Optional;

import com.google.common.base.Preconditions;

public class FhirCodeSystemConcept {

	private final String code;
	private final Optional<String> description;
	private final Optional<String> definition;

	public FhirCodeSystemConcept(String code, String description, String definition) {
		this.code = Preconditions.checkNotNull(code);
		this.description = Optional.ofNullable(description);
		this.definition = Optional.ofNullable(definition);
	}

	public String getCode() {
		return code;
	}
	
	public Optional<String> getDescription() {
		return description;
	}

	public Optional<String> getDefinition() {
		return definition;
	}
	
}
