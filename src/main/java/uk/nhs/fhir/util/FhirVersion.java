package uk.nhs.fhir.util;

import com.google.common.collect.ImmutableList;

public enum FhirVersion {
	DSTU1,
	DSTU2,
	STU3;
	
	private static final ImmutableList<FhirVersion> supportedVersions = ImmutableList.<FhirVersion>builder().add(DSTU2, STU3).build();
	public static ImmutableList<FhirVersion> getSupportedVersions() {
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
