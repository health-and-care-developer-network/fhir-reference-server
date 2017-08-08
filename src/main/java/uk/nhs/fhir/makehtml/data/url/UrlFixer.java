package uk.nhs.fhir.makehtml.data.url;

import uk.nhs.fhir.makehtml.FhirVersion;
import uk.nhs.fhir.makehtml.data.wrap.dstu2.Dstu2UrlFixer;
import uk.nhs.fhir.makehtml.data.wrap.stu3.Stu3UrlFixer;

public abstract class UrlFixer {

	private static final Dstu2UrlFixer dstu2UrlFixer = new Dstu2UrlFixer();
	private static final Stu3UrlFixer stu3UrlFixer = new Stu3UrlFixer();
		
	public static final String fixHL7URL(String hostAndPath, FhirVersion version) {
		switch (version) {
		case DSTU2:
			return dstu2UrlFixer.fixHL7URL(hostAndPath);
		case STU3:
			return stu3UrlFixer.fixHL7URL(hostAndPath);
		default:
			throw new IllegalStateException("No HL7 URL Fixer available for FHIR Version " + version.toString());
		}
	}
	
	protected abstract String fixHL7URL(String hostAndPath);
}
