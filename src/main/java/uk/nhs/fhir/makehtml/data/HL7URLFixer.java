package uk.nhs.fhir.makehtml.data;

import java.util.Set;

import com.google.common.collect.Sets;

import uk.nhs.fhir.makehtml.FhirVersion;

public abstract class HL7URLFixer {
	static final Set<String> extensionStructureDefinitions = 
			Sets.newHashSet("encounter-associatedencounter", "organization-period", "patient-cadavericdonor", "patient-birthtime");
		
	static final Set<String> datatypeStructureDefinitions = Sets.newHashSet("Duration", "Age", "SimpleQuantity");

	private static final DSTU2UrlFixer dstu2UrlFixer = new DSTU2UrlFixer();
	private static final STU3UrlFixer stu3UrlFixer = new STU3UrlFixer();
		
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

	protected String ensureHtmlEnd(String url) {
		if (!url.endsWith(".html")) {
			if (url.contains(".")) {
				throw new IllegalStateException("Unexpected extension: " + url);
			}
			
			url += ".html";
		}
		
		return url;
	}
}
