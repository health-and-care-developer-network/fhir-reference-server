package uk.nhs.fhir.enums;

import ca.uhn.fhir.context.FhirContext;

public enum FHIRVersion {
	DSTU2,
	STU3;
	
	public FhirContext getContext() {
		if (this.equals(FHIRVersion.DSTU2)) {
			return FhirContext.forDstu2();
		} else if (this.equals(FHIRVersion.STU3)) {
			return FhirContext.forDstu3();
		}
		return null;
	}
}
