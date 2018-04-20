package uk.nhs.fhir.data.url;

import uk.nhs.fhir.util.FhirURLConstants;
import uk.nhs.fhir.util.FhirVersion;

/**
 * Created by kevinmayfield on 05/05/2017.
 */
public class ValuesetLinkFix {

	private static final String LEGACY_VALUE_SET_PREFIX = "http://hl7.org/fhir/ValueSet/v2-";
	
    public static String fixLink(String link, FhirVersion version)
    {
        if (link.startsWith(FhirURLConstants.HTTP_HL7_FHIR)) {
          	if (version.equals(FhirVersion.DSTU2)) {
          		link = fixDstu2(link);
          	} else if (version.equals(FhirVersion.STU3)) {
          		link = fixStu3(link);
          	} else {
          		throw new IllegalStateException("Don't know how to fix ValueSet link " + link + " for version " + version.toString());
          	}
        }
        return link;
    }

	public static String fixStu3(String link) {
		if (!link.startsWith(FhirURLConstants.HTTP_HL7_STU3)
		  && !link.startsWith(LEGACY_VALUE_SET_PREFIX)) {
        	if (link.startsWith(FhirURLConstants.HTTP_HL7_VALUESET_V3)) {
        		// e.g. http://hl7.org/fhir/STU3/valueset-v3-FamilyMember.html -> https://www.hl7.org/fhir/STU3/v3/FamilyMember/index.html
        		link = link.replace(FhirURLConstants.HTTP_HL7_VALUESET_V3, FhirURLConstants.HTTP_HL7_STU3_V3 + "/");
        		link += "/vs.html";
        	} else if (link.startsWith(FhirURLConstants.HTTP_HL7_V3)) {
            	// e.g. http://hl7.org/fhir/v3/MaritalStatus -> http://hl7.org/fhir/STU3/v3/MaritalStatus/index.html
                link = link.replace(FhirURLConstants.HTTP_HL7_V3 + "-", FhirURLConstants.HTTP_HL7_STU3_V3);
                link += "/vs.html";
            } else if (link.startsWith(FhirURLConstants.HTTP_HL7_FHIR + "/ValueSet/")) {
            	// e.g. http://hl7.org/fhir/ValueSet/identifier-use -> http://hl7.org/fhir/DSTU2/valueset-identifier-use.html
            	link = link.replace(FhirURLConstants.HTTP_HL7_FHIR + "/ValueSet/", FhirURLConstants.HTTP_HL7_STU3 + "/valueset-");
            	link += ".html";
            }
		}
		
		return link;
	}

	public static String fixDstu2(String link) {
  		if (!link.startsWith(FhirURLConstants.HTTP_HL7_DSTU2)) {
        	if (link.startsWith(FhirURLConstants.HTTP_HL7_VALUESET_V3)) {
        		// e.g. http://hl7.org/fhir/DSTU2/valueset-v3-FamilyMember.html -> https://www.hl7.org/fhir/DSTU2/v3/FamilyMember/index.html
        		link = link.replace(FhirURLConstants.HTTP_HL7_VALUESET_V3, FhirURLConstants.HTTP_HL7_DSTU2_V3 + "/");
        		link += "/index.html";
        	} else if (link.startsWith(FhirURLConstants.HTTP_HL7_V3)) {
            	// e.g. http://hl7.org/fhir/v3/MaritalStatus -> http://hl7.org/fhir/DSTU2/v3/MaritalStatus/index.html
                link = link.replace(FhirURLConstants.HTTP_HL7_V3, FhirURLConstants.HTTP_HL7_DSTU2_V3);
                link += "/index.html";
            } else if (link.startsWith(FhirURLConstants.HTTP_HL7_FHIR + "/ValueSet/")) {
            	// e.g. http://hl7.org/fhir/ValueSet/identifier-use -> http://hl7.org/fhir/DSTU2/valueset-identifier-use.html
            	link = link.replace(FhirURLConstants.HTTP_HL7_FHIR + "/ValueSet/", FhirURLConstants.HTTP_HL7_DSTU2 + "/valueset-");
            	link += ".html";
            } else {
            	// http://hl7.org/fhir/administrative-gender -> http://hl7.org/fhir/DSTU2/valueset-administrative-gender.html
                link = link.replace(FhirURLConstants.HTTP_HL7_FHIR + "/", FhirURLConstants.HTTP_HL7_DSTU2 + "/valueset-");
                link += ".html";
            }
  		}
  		
  		return link;
	}
}
