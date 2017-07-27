package uk.nhs.fhir.makehtml.data;

import uk.nhs.fhir.makehtml.FhirURLConstants;

public class STU3UrlFixer extends HL7URLFixer {

	
	@Override
	public String fixHL7URL(String hostAndPath) {
		if (hostAndPath.toLowerCase().contains("dstu2")) {
			throw new IllegalStateException("STU3 URL " + hostAndPath + " contains string \"dstu2\"");
		}
		
		if (hostAndPath.startsWith(FhirURLConstants.HL7_FHIR)
          && !hostAndPath.startsWith(FhirURLConstants.HL7_STU3)) {
			hostAndPath = hostAndPath.replace(FhirURLConstants.HL7_FHIR, FhirURLConstants.HL7_STU3);
		}
		
		return hostAndPath;
	}
}
