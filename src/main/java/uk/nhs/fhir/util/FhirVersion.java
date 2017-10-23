package uk.nhs.fhir.util;

public enum FhirVersion {
	DSTU1,
	DSTU2,
	STU3;
	
	private static FhirVersion[] supportedVersions = new FhirVersion[]{DSTU2, STU3};
	public static FhirVersion[] getSupportedVersions() {
		return supportedVersions;
	}
	
	public String getUrlPrefix() {
		if (this.equals(DSTU2)) {
			return "";
		} else {
			return toString() + "/";
		}
	}
}
