package uk.nhs.fhir.makehtml.data.structdef;

import java.util.Optional;

public class FhirContact {
	private final String contactData;
	private Optional<Integer> precedence;
	
	public FhirContact(String contactData) {
		this(contactData, null);
	}

	public FhirContact(String contactData, Integer precedence) {
		this.contactData = contactData;
		this.precedence = Optional.ofNullable(precedence);
	}
	
	public String getContactData() {
		return contactData;
	}
	
	public Optional<Integer> getPrecedence() {
		return precedence;
	}
}
