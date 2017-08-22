package uk.nhs.fhir.makehtml.data;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import com.google.common.collect.Sets;

import uk.nhs.fhir.makehtml.FhirURLConstants;
import uk.nhs.fhir.makehtml.NewMain;
import uk.nhs.fhir.makehtml.html.RendererError;

/**
 * Class to wrap a URL allow corrections to URLs used as links, to make them relative.
 */
public class FullFhirURL extends FhirURL {
	
	private static final Set<String> extensionStructureDefinitions = 
		Sets.newHashSet("encounter-associatedencounter", "organization-period", "patient-cadavericdonor", "patient-birthtime");
	
	private static final Set<String> datatypeStructureDefinitions = Sets.newHashSet("Duration", "Age", "SimpleQuantity");
	
	private URL url;
	
	public FullFhirURL(URL url) {
		this.url = url;
	}
	
	public FullFhirURL(String url) throws MalformedURLException {
		this.url = new URL(url);
	}
	
	public String toString() {
		throw new IllegalStateException("choose full string or link string");
	}
	
	public String toFullString() {
		return url.toString();
	}
	
	public String toLinkString() {
		String fullUrl = url.toString();
		
		// convert https://fhir.hl7.org.uk links to relative links so they work in dev environments
		if (NewMain.FHIR_HL7_ORG_LINKS_LOCAL
		  && fullUrl.startsWith(FhirURLConstants.HTTP_FHIR_HL7_ORG_UK)) {
			return fullUrl.substring(FhirURLConstants.HTTP_FHIR_HL7_ORG_UK.length());
		}
		
		// split scheme off the front, if present
		String schemeEnd = "://";
		
		String scheme;
		String hostAndPath;
		int schemeEndIndex = fullUrl.indexOf(schemeEnd);
		if (schemeEndIndex > -1) {
			schemeEndIndex += schemeEnd.length();
			scheme = fullUrl.substring(0, schemeEndIndex);
			hostAndPath = fullUrl.substring(scheme.length());
		} else {
			scheme = "http://";
			hostAndPath = fullUrl;
		}
		
		// fix up hl7.org/ hosts, excluding hl7.org.uk/ hosts
		if (hostAndPath.contains(FhirURLConstants.HL7_ROOT + "/")) {
			hostAndPath = fixHL7URL(hostAndPath);
		}
		
		String linkUrl = scheme + hostAndPath;
		
		if (isLogicalUrl(linkUrl)) {
			RendererError.handle(RendererError.Key.LINK_WITH_LOGICAL_URL, "Using logical url " + linkUrl + " as a link href");
		} else if (NewMain.TEST_LINK_URLS) {
			FhirURL.addLinkUrl(linkUrl);
		}
		
		return linkUrl;
	}
	
	private String fixHL7URL(String hostAndPath) {
		// http://hl7.org/anything_else/... -> http://hl7.org/dstu2/anything_else/...
		if (hostAndPath.startsWith(FhirURLConstants.HL7_FHIR)
          && !hostAndPath.startsWith(FhirURLConstants.HL7_DSTU2)) {
			hostAndPath = hostAndPath.replace(FhirURLConstants.HL7_FHIR, FhirURLConstants.HL7_DSTU2);
		}
		
		// sanity check
		if (hostAndPath.contains(FhirURLConstants.HL7_ROOT)
		  && !hostAndPath.contains(FhirURLConstants.FHIR_HL7_ORG_UK) // ignore hl7.org.uk/, which doesn't need dstu2
		  && !hostAndPath.contains("dstu2")
		  && !hostAndPath.contains("DSTU2")) {
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
