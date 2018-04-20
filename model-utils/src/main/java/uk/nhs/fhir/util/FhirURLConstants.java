package uk.nhs.fhir.util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.StreamSupport;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class FhirURLConstants {
	
	public static final String SCHEME_END = "://";
	public static final String SCHEME_HTTP = "http" + SCHEME_END;
	public static final String SCHEME_HTTPS = "https" + SCHEME_END;
	
	public static final String HL7_ORG_DOMAIN = "hl7.org";
	public static final String HL7_FHIR = HL7_ORG_DOMAIN + "/fhir";
	public static final String HL7_DSTU2 = HL7_FHIR + "/DSTU2";
	public static final String HL7_STU3 = HL7_FHIR + "/stu3";
	public static final String HL7_VALUESET = HL7_FHIR + "/ValueSet";
	public static final String HL7_VALUESET_V3 = HL7_VALUESET + "/v3-";
	public static final String HL7_DSTU2_VALUESET = HL7_DSTU2 + "/ValueSet";
	public static final String HL7_DSTU2_STRUCTURE_DEF = HL7_DSTU2 + "/StructureDefinition";
	public static final String HL7_V3 = HL7_FHIR + "/v3";
	public static final String HL7_DSTU2_V3 = HL7_DSTU2 + "/v3";
	public static final String HL7_STU3_V3 = HL7_STU3 + "/v3";
	
	public static final String HTTP_HL7_ROOT = SCHEME_HTTP + HL7_ORG_DOMAIN;
	public static final String HTTP_HL7_FHIR = SCHEME_HTTP + HL7_FHIR;
	public static final String HTTP_HL7_DSTU2 = SCHEME_HTTP + HL7_DSTU2;
	public static final String HTTP_HL7_STU3 = SCHEME_HTTP + HL7_STU3;
	public static final String HTTP_HL7_VALUESET_V3 = SCHEME_HTTP + HL7_VALUESET_V3;
	public static final String HTTP_HL7_V3 = SCHEME_HTTP + HL7_V3;
	public static final String HTTP_HL7_DSTU2_V3 = SCHEME_HTTP + HL7_DSTU2_V3;
	public static final String HTTP_HL7_STU3_V3 = SCHEME_HTTP + HL7_STU3_V3;

	public static final String HL7_TERMINOLOGIES = HTTP_HL7_FHIR + "/terminologies.html";
	
	public static final String HL7_CONFORMANCE = HTTP_HL7_FHIR + "/conformance-rules.html";
	public static final String HL7_DATATYPES = HTTP_HL7_FHIR + "/datatypes.html";
	public static final String HL7_SEARCH = HTTP_HL7_FHIR + "/search.html";
	public static final String HL7_FORMATS = HTTP_HL7_FHIR + "/formats.html";
	
	public static final String FHIR_NHS_NET_DOMAIN = "fhir.nhs.net";
	public static final String FHIR_NHS_NET_QDOMAIN = SCHEME_HTTP + FHIR_NHS_NET_DOMAIN;
	
	public static final String FHIR_HL7_ORG_UK_DOMAIN = "fhir.hl7.org.uk";
	public static final String FHIR_HL7_ORG_UK_QDOMAIN = SCHEME_HTTPS + FHIR_HL7_ORG_UK_DOMAIN;

	public static final String FHIR_NHS_UK_DOMAIN = "fhir.nhs.uk";
	public static final String FHIR_NHS_UK_QDOMAIN = SCHEME_HTTPS + FHIR_NHS_UK_DOMAIN;
	
	public static final String FHIR_TEST_NHS_UK_DOMAIN = "fhir-test.nhs.uk";
	public static final String FHIR_TEST_NHS_UK_QDOMAIN = SCHEME_HTTPS + FHIR_TEST_NHS_UK_DOMAIN;
	public static final String FHIR_TEST_HL7_NHS_UK_DOMAIN = "fhir-test.hl7.org.uk";
	public static final String FHIR_TEST_HL7_NHS_UK_QDOMAIN = SCHEME_HTTPS + FHIR_TEST_HL7_NHS_UK_DOMAIN;
	
	public static final String NHS_LOGICAL_URL_PREFIX = "/Id/";
	public static final String NHS_ID = FHIR_NHS_UK_QDOMAIN + NHS_LOGICAL_URL_PREFIX;
	public static final String NHS_FHIR_IMAGES_DIR = FHIR_NHS_UK_QDOMAIN + "/images";
	
	public static final String SNOMED_ID = "http://snomed.info/sct";

	public static String versionBase(FhirVersion version) {
		switch(version) {
			case DSTU2:
				return HTTP_HL7_DSTU2;
			case STU3:
				return HTTP_HL7_STU3;
			default:
				throw new IllegalStateException("Base URL for version " + version.toString());
		}
	}
	
	public static String trimNhsUrlPrefix(String url) {
		if (isNhsResourceUrl(url)) {
			return trimNhsQualifiedPrefix(url);
		} else if (startsWithNhsDomain(url)) {
			return trimNhsDomainPrefix(url);
		} else {
			return url;
		}
	}
	
	private static final String[] NHS_PROFILE_PREFIXES = new String[]{FHIR_NHS_NET_QDOMAIN, FHIR_NHS_UK_QDOMAIN, FHIR_HL7_ORG_UK_QDOMAIN, FHIR_TEST_NHS_UK_QDOMAIN, FHIR_TEST_HL7_NHS_UK_QDOMAIN};
	public static boolean isNhsResourceUrl(String url) {
		return StreamSupport
			.stream(Arrays.spliterator(NHS_PROFILE_PREFIXES), false)
			.anyMatch(prefix -> url.startsWith(prefix));
	}
	public static String trimNhsQualifiedPrefix(String url) {
		for (String qualifiedPrefix : NHS_PROFILE_PREFIXES) {
			if (url.startsWith(qualifiedPrefix)) {
				return url.substring(qualifiedPrefix.length());
			}
		}
		
		throw new IllegalStateException("Expected url (" + url + ") to start with an NHS qualified URL prefix");
	}
	
	private static final String[] NHS_PROFILE_DOMAINS = new String[] {FHIR_NHS_NET_DOMAIN, FHIR_NHS_UK_DOMAIN, FHIR_HL7_ORG_UK_DOMAIN, FHIR_TEST_NHS_UK_DOMAIN, FHIR_TEST_HL7_NHS_UK_DOMAIN};
	public static boolean startsWithNhsDomain(String url) {
		return StreamSupport
				.stream(Arrays.spliterator(NHS_PROFILE_DOMAINS), false)
				.anyMatch(prefix -> url.startsWith(prefix));
	}
	
	public static String trimNhsDomainPrefix(String url) {
		for (String domain : NHS_PROFILE_DOMAINS) {
			if (url.startsWith(domain)) {
				return url.substring(domain.length());
			}
		}
		
		throw new IllegalStateException("Expected url (" + url + ") to start with an NHS profile domain");
	}

    public static final Set<String> DEFAULT_LOCAL_QDOMAINS = ImmutableSet.copyOf(Sets.newHashSet(
    	// some GPConnect DSTU2 resources
		FhirURLConstants.FHIR_NHS_NET_QDOMAIN,
		// test NHSD site
		FhirURLConstants.FHIR_TEST_NHS_UK_QDOMAIN,
		// live NHSD site
		FhirURLConstants.FHIR_NHS_UK_QDOMAIN,
		// test HL7 site
		FhirURLConstants.FHIR_TEST_HL7_NHS_UK_QDOMAIN,
		// live HL7 site
		FhirURLConstants.FHIR_HL7_ORG_UK_QDOMAIN
    ));
}
