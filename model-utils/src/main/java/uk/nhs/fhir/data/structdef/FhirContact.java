package uk.nhs.fhir.data.structdef;

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

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		
		if (!(other instanceof FhirContact)) {
			return false;
		}
		
		FhirContact otherFhirContact = (FhirContact)other;
		
		return contactData.equals(otherFhirContact.getContactData())
		  && precedence.equals(otherFhirContact.getPrecedence());
	}
	
	@Override
	public int hashCode() {
		return contactData.hashCode() * precedence.hashCode();
	}

	@Override
	public String toString() {
		return "FhirContact[" + contactData + " [" + precedence.map(i -> Integer.toString(i)).orElse("") + "]"; 
	}
}
