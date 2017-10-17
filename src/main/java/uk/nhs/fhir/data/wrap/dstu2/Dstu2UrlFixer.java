package uk.nhs.fhir.data.wrap.dstu2;

import java.util.Set;

import com.google.common.collect.Sets;

import uk.nhs.fhir.data.FhirURLConstants;
import uk.nhs.fhir.data.url.UrlFixer;
import uk.nhs.fhir.makehtml.RendererError;

public class Dstu2UrlFixer extends UrlFixer {

	private static final Set<String> extensionStructureDefinitions = 
			Sets.newHashSet("encounter-associatedencounter", "organization-period", "patient-cadavericdonor", "patient-birthtime");
		
	private static final Set<String> datatypeStructureDefinitions = Sets.newHashSet("Duration", "Age", "SimpleQuantity");
	
	@Override
	public String fixHL7URL(String hostAndPath) {
		// http://hl7.org/fhir/anything_else/... -> http://hl7.org/fhir/dstu2/anything_else/...
		if (hostAndPath.startsWith(FhirURLConstants.HL7_FHIR)
          && !hostAndPath.startsWith(FhirURLConstants.HL7_DSTU2)) {
			hostAndPath = hostAndPath.replace(FhirURLConstants.HL7_FHIR, FhirURLConstants.HL7_DSTU2);
		}
		
		// sanity check
		if (hostAndPath.contains(FhirURLConstants.HL7_ROOT + "/")
		  && !hostAndPath.toLowerCase().contains("dstu2")) {
			RendererError.handle(RendererError.Key.HL7_URL_WITHOUT_DSTU2, "Should " + hostAndPath + " have been modified to contain /dstu2/ ?");
			
			if (hostAndPath.contains(FhirURLConstants.HL7_FHIR)) {
				hostAndPath = hostAndPath.replace(FhirURLConstants.HL7_FHIR, FhirURLConstants.HL7_DSTU2);
			} else {
				hostAndPath = hostAndPath.replace(FhirURLConstants.HL7_ROOT, FhirURLConstants.HL7_DSTU2);
			}
		}
		
		// fix up structure def to have .html extension (otherwise they redirect to non dstu2)
		// fix up extensions and datatypes (otherwise they redirect to non dstu2)
		if (hostAndPath.startsWith(FhirURLConstants.HL7_DSTU2_STRUCTURE_DEF)) {
			String urlEnd = hostAndPath.substring(FhirURLConstants.HL7_DSTU2_STRUCTURE_DEF.length() + 1);
			
			if (extensionStructureDefinitions.contains(urlEnd.toLowerCase())) {
				urlEnd = "extension-" + urlEnd.toLowerCase() + ".html";
			} else if (datatypeStructureDefinitions.contains(urlEnd)) {
				urlEnd = "datatypes.html#" + urlEnd.toLowerCase();
			} else {
				urlEnd = ensureHtmlEnd(urlEnd);
			}
			
			hostAndPath = FhirURLConstants.HL7_DSTU2 + "/" + urlEnd;
		}
		
		if (hostAndPath.startsWith(FhirURLConstants.HL7_DSTU2_VALUESET)) {
			String urlEnd = hostAndPath.substring(FhirURLConstants.HL7_DSTU2_VALUESET.length() + 1);
			hostAndPath = FhirURLConstants.HL7_DSTU2 + "/valueset-" + ensureHtmlEnd(urlEnd);
		}
		
		return hostAndPath;
	}

	private String ensureHtmlEnd(String url) {
		if (!url.endsWith(".html")) {
			if (url.contains(".")) {
				throw new IllegalStateException("Unexpected extension: " + url);
			}
			
			url += ".html";
		}
		
		return url;
	}
}
