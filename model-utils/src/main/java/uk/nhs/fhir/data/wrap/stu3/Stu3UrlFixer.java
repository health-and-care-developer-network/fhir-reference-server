package uk.nhs.fhir.data.wrap.stu3;

import java.util.Locale;

import uk.nhs.fhir.data.url.UrlFixer;
import uk.nhs.fhir.util.FhirURLConstants;

public class Stu3UrlFixer extends UrlFixer {
	
	@Override
	public String fixHL7URL(String hostAndPath) {
		if (hostAndPath.toLowerCase(Locale.UK).contains("dstu2")) {
			throw new IllegalStateException("STU3 URL " + hostAndPath + " contains string \"dstu2\"");
		}
		
		if (hostAndPath.startsWith(FhirURLConstants.HL7_FHIR)
          && !hostAndPath.startsWith(FhirURLConstants.HL7_STU3)) {
			hostAndPath = hostAndPath.replace(FhirURLConstants.HL7_FHIR, FhirURLConstants.HL7_STU3);
		}
		
		return hostAndPath;
	}
}
