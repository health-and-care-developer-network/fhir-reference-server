package uk.nhs.fhir.data.codesystem;

import java.util.Optional;

public class FhirIdentifier {
	private final Optional<String> value;
	private final Optional<String> system;
	
	public FhirIdentifier(String value, String system) {
		this.value = Optional.ofNullable(value);
		this.system = Optional.ofNullable(system);
	}
	
	public Optional<String> getValue() {
		return value;
	}
	
	public Optional<String> getSystem() {
		return system;
	}
}
