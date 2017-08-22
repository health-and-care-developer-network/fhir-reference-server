package uk.nhs.fhir.data.codesystem;

import java.util.Optional;

public class FhirIdentifier {
	private final Optional<String> type;
	private final Optional<String> system;
	
	public FhirIdentifier(String type, String system) {
		this.type = Optional.ofNullable(type);
		this.system = Optional.ofNullable(system);
	}
	
	public Optional<String> getType() {
		return type;
	}
	
	public Optional<String> getSystem() {
		return system;
	}
}
