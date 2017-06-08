package uk.nhs.fhir.makehtml;

public interface HTMLConstants {
	public static final String NHS_DEVELOPER_IMAGES_DIR = "http://data.developer.nhs.uk/fhir/candidaterelease-170816-getrecord/dist/images/";
	public static final String NHS_FHIR_IMAGES_DIR = "https://fhir.nhs.uk/images/";
	public static final String HL7_ROOT = "http://www.hl7.org";
	public static final String HL7_FHIR = HL7_ROOT + "/fhir";
	public static final String HL7_DSTU2 = HL7_FHIR + "/DSTU2";
	
	public static final String HL7_CONFORMANCE = HL7_DSTU2 + "/conformance-rules.html";
	public static final String HL7_DATATYPES = HL7_DSTU2 + "/datatypes.html";
	public static final String HL7_TERMINOLOGIES = HL7_DSTU2 + "/terminologies.html";
	public static final String HL7_SEARCH = HL7_DSTU2 + "/search.html";
	public static final String HL7_FORMATS = HL7_DSTU2 + "/formats.html";
	
	public static final String FHIR_HL7_ORG_UK = "https://fhir.hl7.org.uk";
}
