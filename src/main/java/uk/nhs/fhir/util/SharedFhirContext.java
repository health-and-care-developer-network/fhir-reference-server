package uk.nhs.fhir.util;

import ca.uhn.fhir.context.FhirContext;

public class SharedFhirContext {
	private static final FhirContext context = FhirContext.forDstu2();
	
	private SharedFhirContext(){}
	
	public static FhirContext get() {
		return context;
	}
}
