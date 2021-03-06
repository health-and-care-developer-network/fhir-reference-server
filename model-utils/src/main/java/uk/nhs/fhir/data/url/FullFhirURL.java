package uk.nhs.fhir.data.url;

import java.net.MalformedURLException;
import java.net.URL;

import uk.nhs.fhir.data.wrap.dstu2.Dstu2UrlFixer;
import uk.nhs.fhir.data.wrap.stu3.Stu3UrlFixer;
import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;
import uk.nhs.fhir.util.FhirURLConstants;
import uk.nhs.fhir.util.FhirVersion;

/**
 * Class to wrap a URL allow corrections to URLs used as links, to make them relative.
 */
public class FullFhirURL extends FhirURL {

	// send requests to linked external pages and check the response. If false, use cached values where necessary. 
	public static final boolean TEST_LINK_URLS = false;

	private static final UrlFixer dstu2UrlFixer = new Dstu2UrlFixer();
	private static final UrlFixer stu3UrlFixer = new Stu3UrlFixer();
	
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
		
		// This conversion now performed below, after splitting off the scheme
		// convert https://fhir.hl7.org.uk links to relative links so they work in dev environments
		//if (FHIR_HL7_ORG_LINKS_LOCAL
		//  && fullUrl.startsWith(FhirURLConstants.HTTPS_FHIR_HL7_ORG_UK)) {	
		//	return fullUrl.substring(FhirURLConstants.HTTPS_FHIR_HL7_ORG_UK.length());
		//}
		
		// split scheme off the front, if present	
		String scheme;
		String hostAndPath;
		int schemeEndIndex = fullUrl.indexOf(FhirURLConstants.SCHEME_END);
		if (schemeEndIndex > -1) {
			schemeEndIndex += FhirURLConstants.SCHEME_END.length();
			scheme = fullUrl.substring(0, schemeEndIndex);
			hostAndPath = fullUrl.substring(scheme.length());
		} else {
			scheme = FhirURLConstants.SCHEME_HTTP;
			hostAndPath = fullUrl;
		}
		
		// fix up hl7.org/ hosts, excluding hl7.org.uk/ hosts
		if (hostAndPath.contains(FhirURLConstants.HL7_ORG_DOMAIN + "/")) {
			hostAndPath = fixHL7URL(hostAndPath);
		}
		
		String linkUrl;
		if (FhirURL.startsWithLocalDomain(hostAndPath)) {
			// make it local
			linkUrl = FhirURL.trimLocalDomainPrefix(hostAndPath);
		} else {
			// restore the scheme
			linkUrl = scheme + hostAndPath;
		}
		
		if (isLogicalUrl(linkUrl)) {
			EventHandlerContext.forThread().event(RendererEventType.LINK_WITH_LOGICAL_URL, "Using logical url " + linkUrl + " as a link href");
		} else if (TEST_LINK_URLS) {
			FhirURL.addLinkUrl(linkUrl);
		}
		
		return linkUrl;
	}

	private String fixHL7URL(String hostAndPath) {
		if (version.equals(FhirVersion.DSTU2)) {
			return dstu2UrlFixer.fixHL7URL(hostAndPath);
		} else if (version.equals(FhirVersion.STU3)) {
			return stu3UrlFixer.fixHL7URL(hostAndPath);
		} else {
			throw new IllegalStateException("No URL fixer for version " + version.toString());
		}
	}
}
