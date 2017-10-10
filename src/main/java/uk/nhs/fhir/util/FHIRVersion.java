package uk.nhs.fhir.util;

import ca.uhn.fhir.context.FhirContext;

public enum FHIRVersion {
	DSTU2(""),
	STU3("STU3/");
	
	private String urlPrefix = null;
	private static FhirContext fhirContextDSTU2 = FhirContext.forDstu2();
	private static FhirContext fhirContextSTU3 = FhirContext.forDstu3();
	
	private FHIRVersion(String urlPrefix) {
		this.urlPrefix = urlPrefix;
	}
	
	public FhirContext getContext() {
		if (this.equals(FHIRVersion.DSTU2)) {
			return fhirContextDSTU2;
		} else if (this.equals(FHIRVersion.STU3)) {
			return fhirContextSTU3;
		}
		return null;
	}

	public final String getUrlPrefix() {
		return urlPrefix;
	}
}
