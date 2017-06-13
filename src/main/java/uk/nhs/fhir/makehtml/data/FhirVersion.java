package uk.nhs.fhir.makehtml.data;

public enum FhirVersion {
	v1_0_1("DSTU2");
	
	private static final String v1_0_1_Str = "1.0.1";
	
	private final String desc;
	
	FhirVersion(String desc) {
		this.desc = desc;
	}
	
	public static FhirVersion forString(String versionString) {
		if (versionString.equals(v1_0_1_Str)) {
			return v1_0_1;
		}
		
		throw new IllegalStateException("No FhirVersion mapped for " + versionString);
	}

	public String getDesc() {
		return desc;
	}
}
