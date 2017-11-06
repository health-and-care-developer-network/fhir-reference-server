package uk.nhs.fhir;

import uk.nhs.fhir.util.FhirVersion;

public class FhirURLConstants {
	public static final String HL7_ROOT = "hl7.org";
	public static final String HL7_FHIR = HL7_ROOT + "/fhir";
	public static final String HL7_DSTU2 = HL7_FHIR + "/DSTU2";
	public static final String HL7_STU3 = HL7_FHIR + "/stu3";
	public static final String HL7_VALUESET = HL7_FHIR + "/ValueSet";
	public static final String HL7_VALUESET_V3 = HL7_VALUESET + "/v3-";
	public static final String HL7_DSTU2_VALUESET = HL7_DSTU2 + "/ValueSet";
	public static final String HL7_DSTU2_STRUCTURE_DEF = HL7_DSTU2 + "/StructureDefinition";
	public static final String HL7_V3 = HL7_FHIR + "/v3";
	public static final String HL7_DSTU2_V3 = HL7_DSTU2 + "/v3";
	public static final String HL7_STU3_V3 = HL7_STU3 + "/v3";
	
	public static final String HTTP_HL7_ROOT = "http://" + HL7_ROOT;
	public static final String HTTP_HL7_FHIR = "http://" + HL7_FHIR;
	public static final String HTTP_HL7_DSTU2 = "http://" + HL7_DSTU2;
	public static final String HTTP_HL7_STU3 = "http://" + HL7_STU3;
	public static final String HTTP_HL7_VALUESET_V3 = "http://" + HL7_VALUESET_V3;
	public static final String HTTP_HL7_V3 = "http://" + HL7_V3;
	public static final String HTTP_HL7_DSTU2_V3 = "http://" + HL7_DSTU2_V3;
	public static final String HTTP_HL7_STU3_V3 = "http://" + HL7_STU3_V3;

	public static final String HL7_TERMINOLOGIES = HTTP_HL7_FHIR + "/terminologies.html";
	
	public static final String HL7_CONFORMANCE = HTTP_HL7_FHIR + "/conformance-rules.html";
	public static final String HL7_DATATYPES = HTTP_HL7_FHIR + "/datatypes.html";
	public static final String HL7_SEARCH = HTTP_HL7_FHIR + "/search.html";
	public static final String HL7_FORMATS = HTTP_HL7_FHIR + "/formats.html";
	
	public static final String FHIR_HL7_ORG_UK = "fhir.hl7.org.uk";
	public static final String HTTPS_FHIR_HL7_ORG_UK = "https://" + FHIR_HL7_ORG_UK;

	public static final String FHIR_NHS_UK = "https://fhir.nhs.uk";
	public static final String NHS_ID = FHIR_NHS_UK + "/Id";
	public static final String NHS_FHIR_IMAGES_DIR = FHIR_NHS_UK + "/images";
	
	public static final String SNOMED_ID = "http://snomed.info/sct";

	public static String versionBase(FhirVersion version) {
		switch(version) {
			case DSTU2:
				return HTTP_HL7_DSTU2;
			case STU3:
				return HTTP_HL7_STU3;
			default:
				throw new IllegalStateException("Base URL for version " + version.toString());
		}
	}
	
	public static final String[] NHS_PROFILE_PREFIXES = new String[]{"http://fhir.nhs.net", FHIR_NHS_UK, HTTPS_FHIR_HL7_ORG_UK};
	public static boolean isNhsResourceUrl(String url) {
		for (String prefix : NHS_PROFILE_PREFIXES) {
			if (url.startsWith(prefix)) {
				return true;
			}
		}
		
		return false;
	}
}
