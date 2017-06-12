package uk.nhs.fhir.makehtml.html;

import uk.nhs.fhir.makehtml.FhirURLConstants;

/**
 * Created by kevinmayfield on 05/05/2017.
 */
public class Dstu2Fix {

    public static String valuesetDstu2links(String value)
    {
        if (value.startsWith(FhirURLConstants.HL7_FHIR)
          && !value.startsWith(FhirURLConstants.HL7_DSTU2)) {
        	
            if (value.startsWith(FhirURLConstants.HL7_V3)) {
            	// e.g. http://hl7.org/fhir/v3/MaritalStatus -> http://hl7.org/fhir/DSTU2/v3/MaritalStatus/index.html
                value = value.replace(FhirURLConstants.HL7_V3, FhirURLConstants.HL7_DSTU2_V3);
                value += "/index.html";
            } else if (value.startsWith(FhirURLConstants.HL7_FHIR + "/ValueSet/")) {
            	// e.g. http://hl7.org/fhir/ValueSet/identifier-use -> http://hl7.org/fhir/DSTU2/valueset-identifier-use.html
            	value = value.replace(FhirURLConstants.HL7_FHIR + "/ValueSet/", FhirURLConstants.HL7_DSTU2 + "/valueset-");
            	value += ".html";
            } else {
                value = value.replace(FhirURLConstants.HL7_FHIR + "/", FhirURLConstants.HL7_DSTU2 + "/valueset-");
                value += ".html";
            }
        }
        return value;
    }
}
