package uk.nhs.fhir.makehtml;

public interface FhirURLConstants {
	public static final String NHS_FHIR_IMAGES_DIR = "https://fhir.nhs.uk/images/";
	public static final String HL7_ROOT = "http://hl7.org";
	public static final String HL7_FHIR = HL7_ROOT + "/fhir";
	public static final String HL7_DSTU2 = HL7_FHIR + "/DSTU2";
	public static final String HL7_V3 = HL7_FHIR + "/v3";
	public static final String HL7_DSTU2_V3 = HL7_DSTU2 + "/v3";
	
	public static final String HL7_CONFORMANCE = HL7_DSTU2 + "/conformance-rules.html";
	public static final String HL7_DATATYPES = HL7_DSTU2 + "/datatypes.html";
	public static final String HL7_TERMINOLOGIES = HL7_DSTU2 + "/terminologies.html";
	public static final String HL7_SEARCH = HL7_DSTU2 + "/search.html";
	public static final String HL7_FORMATS = HL7_DSTU2 + "/formats.html";
	
	public static final String FHIR_HL7_ORG_UK = "https://fhir.hl7.org.uk";
	
	public static final String FHIR_NHS_UK = "https://fhir.nhs.uk/";
}
