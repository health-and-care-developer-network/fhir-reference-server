package uk.nhs.fhir.makehtml.data;

import java.net.MalformedURLException;
import java.net.URL;

import uk.nhs.fhir.makehtml.FhirURLConstants;
import uk.nhs.fhir.makehtml.FhirVersion;
import uk.nhs.fhir.makehtml.NewMain;
import uk.nhs.fhir.makehtml.html.RendererError;

/**
 * Class to wrap a URL allow corrections to URLs used as links, to make them relative.
 */
public class FullFhirURL extends FhirURL {
	private static final String SCHEME_END = "://";
	
	private URL url;
	private final FhirVersion version;
	
	public FullFhirURL(String url, FhirVersion version) throws MalformedURLException {
		this.url = new URL(url);
		this.version = version;
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
		String scheme;
		String hostAndPath;
		int schemeEndIndex = fullUrl.indexOf(SCHEME_END);
		if (schemeEndIndex > -1) {
			schemeEndIndex += SCHEME_END.length();
			scheme = fullUrl.substring(0, schemeEndIndex);
			hostAndPath = fullUrl.substring(scheme.length());
		} else {
			scheme = "http://";
			hostAndPath = fullUrl;
		}
		
		// fix up hl7.org/ hosts, excluding hl7.org.uk/ hosts
		if (hostAndPath.contains(FhirURLConstants.HL7_ROOT + "/")) {
			hostAndPath = HL7URLFixer.fixHL7URL(hostAndPath, version);
		}
		
		// restore the scheme
		String linkUrl = scheme + hostAndPath;
		
		if (isLogicalUrl(linkUrl)) {
			RendererError.handle(RendererError.Key.LINK_WITH_LOGICAL_URL, "Using logical url " + linkUrl + " as a link href");
		} else if (NewMain.TEST_LINK_URLS) {
			FhirURL.addLinkUrl(linkUrl);
		}
		
		return linkUrl;
	}
}
