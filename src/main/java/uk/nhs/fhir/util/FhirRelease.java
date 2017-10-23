package uk.nhs.fhir.util;

/**
 * Compiled from list on http://hl7.org/fhir/history.html
 */
public enum FhirRelease {
	v0_1_0("0.1.0", FhirVersion.DSTU1),
	v0_2_0("0.2.0", FhirVersion.DSTU1),
	v0_2_1("0.2.1", FhirVersion.DSTU1),
	v0_3_0("0.3.0", FhirVersion.DSTU1),
	v0_4_0("0.4.0", FhirVersion.DSTU1),
	v0_5_0("0.5.0", FhirVersion.DSTU1),
	v1_0_0("1.0.0", FhirVersion.DSTU2),
	v1_0_1("1.0.1", FhirVersion.DSTU2),
	v1_0_2("1.0.2", FhirVersion.DSTU2),
	v1_1_0("1.1.0", FhirVersion.DSTU2),
	v1_2_0("1.2.0", FhirVersion.DSTU2),
	v1_4_0("1.4.0", FhirVersion.DSTU2),
	v1_5_0("1.5.0", FhirVersion.DSTU2),
	v1_6_0("1.6.0", FhirVersion.STU3),
	v1_8_0("1.8.0", FhirVersion.STU3),
	v1_9_0("1.9.0", FhirVersion.STU3),
	v3_0_0("3.0.0", FhirVersion.STU3),
	v3_0_1("3.0.1", FhirVersion.STU3);
	
	private final String releaseString;
	private final FhirVersion version;
	
	FhirRelease(String versionString, FhirVersion desc) {
		this.releaseString = versionString;
		this.version = desc;
	}
	
	public static FhirRelease forString(String releaseString) {
		for (FhirRelease release : FhirRelease.values()) {
			if (releaseString.equals(release.getReleaseString())) {
				return release;
			}
		}
		
		throw new IllegalStateException("No FhirRelease mapped for " + releaseString);
	}

	public FhirVersion getVersion() {
		return version;
	}
	
	public String getDesc() {
		return version.toString();
	}

	public String getReleaseString() {
		return releaseString;
	}
}
