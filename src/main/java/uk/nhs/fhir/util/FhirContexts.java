package uk.nhs.fhir.util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class FhirContexts {
	private static final FhirContext DSTU2_CONTEXT = FhirContext.forDstu2();
	private static final FhirContext DSTU3_CONTEXT = FhirContext.forDstu3();
	
	// never instantiated
	private FhirContexts(){}
	
	public static FhirContext forVersion(FhirVersion fhirVersion) {
		switch (fhirVersion) {
		case DSTU2:
			return DSTU2_CONTEXT;
		case STU3:
			return DSTU3_CONTEXT;
		default:
			throw new IllegalStateException("No context/parser available for " + fhirVersion.toString());
		}
	}

	public static IParser xmlParser(FhirVersion fhirVersion) {
		return forVersion(fhirVersion).newXmlParser();
	}
}
