package uk.nhs.fhir.enums;

import ca.uhn.fhir.context.FhirContext;

public enum FHIRVersion {
	DSTU2("", "<span class='dstu2'>DSTU2</span>"),
	STU3("3.0.1/", "<span class='stu3'>STU3</span>");
	
	private String urlPrefix = null;
	private String label = null;
	private static FhirContext fhirContextDSTU2 = FhirContext.forDstu2();
	private static FhirContext fhirContextSTU3 = FhirContext.forDstu3();
	
	private FHIRVersion(String urlPrefix, String label) {
		this.urlPrefix = urlPrefix;
		this.label = label;
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

	public String getLabel() {
		return label;
	}
}
