package uk.nhs.fhir.data.structdef;

import java.util.Optional;

public class Example {
	private final Optional<String> label;
	private String value;
	
	public Example(String value, Optional<String> label) {
		this.value = value;
		this.label = label;
	}
	
	public String getValue() {
		return value;
	}
	
	public Optional<String> getLabel() {
		return label;
	}
}
