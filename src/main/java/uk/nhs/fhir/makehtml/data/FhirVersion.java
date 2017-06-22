package uk.nhs.fhir.makehtml.data;

public enum FhirVersion {
	v1_0_1("1.0.1", "DSTU2"),
	v1_0_2("1.0.2", "DSTU2");
	
	private final String versionString;
	private final String desc;
	
	FhirVersion(String versionString, String desc) {
		this.versionString = versionString;
		this.desc = desc;
	}
	
	public static FhirVersion forString(String versionString) {
		for (FhirVersion version : FhirVersion.values()) {
			if (versionString.equals(version.getVersionString())) {
				return version;
			}
		}
		
		throw new IllegalStateException("No FhirVersion mapped for " + versionString);
	}

	public String getDesc() {
		return desc;
	}

	public String getVersionString() {
		return versionString;
	}
}
