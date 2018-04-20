package uk.nhs.fhir.data.url;

import java.net.MalformedURLException;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import uk.nhs.fhir.util.FhirURLConstants;
import uk.nhs.fhir.util.FhirVersion;

public abstract class FhirURL {
	
	// The set of domains considered 'local', i.e. hosted on the same server
	private static Set<String> localQDomains = FhirURLConstants.DEFAULT_LOCAL_QDOMAINS;
	
	private static Set<String> domainsFromQDomains(Set<String> qDomains) { 
    	return ImmutableSet.copyOf(qDomains.stream()
    		.map(qdomain -> qdomain.substring(qdomain.indexOf("://") + "://".length()))
    		.collect(Collectors.toSet()));
	}
	
	private static Set<String> localDomains = domainsFromQDomains(localQDomains);
	
	public static Set<String> getLocalQDomains() {
		return FhirURL.localQDomains;
	}
	
	public static void setLocalQDomains(Set<String> localQDomains) {
		FhirURL.localQDomains = ImmutableSet.copyOf(localQDomains);
		FhirURL.localDomains = domainsFromQDomains(localQDomains);
	}
	
	public static boolean startsWithLocalQDomain(String url) {
		return localQDomains.stream().anyMatch(qd -> url.startsWith(qd));
	}

	public static boolean startsWithLocalDomain(String url) {
		return localDomains.stream().anyMatch(domain -> url.startsWith(domain));
	}
	
	public static String trimLocalDomainPrefix(String url) {
		for (String domain : localDomains) {
			if (url.startsWith(domain)) {
				return url.substring(domain.length());
			}
		}
		
		throw new IllegalStateException("Expected url (" + url + ") to start with local Domain");
	}
	
	public abstract String toLinkString();
	public abstract String toFullString();

	private static final Set<String> linkUrls = Sets.newHashSet();
	public static Set<String> getLinkUrls() {
		return linkUrls;
	}
	public static void addLinkUrl(String url) {
		linkUrls.add(url);
	}

	public static FhirURL buildOrThrow(String url, FhirVersion version) {
		try {
			return new FullFhirURL(url, version);
		} catch (MalformedURLException e) {
			try {
				return new RelativeFhirUrl(url);
			} catch (IllegalArgumentException e2) {
				throw new IllegalStateException("Not a valid FHIR URL string: " + url);
			}
		}
	}
	
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		
		if (!(other instanceof FhirURL)) {
			return false;
		}
		
		FhirURL otherFhirUrl = (FhirURL)other;
		
		return toLinkString().equals(otherFhirUrl.toLinkString());
	}
	
	public int hashCode() {
		return toLinkString().hashCode();
	}

	/**
	 * Some urls are used as logical identities and are not intended to be used as links.
	 * In this case they probably shouldn't be being used as a link anyway.
	 */
	public static boolean isLogicalUrl(String url) {
		return url.startsWith(FhirURLConstants.NHS_ID)
		  || FhirURLConstants.trimNhsUrlPrefix(url).startsWith(FhirURLConstants.NHS_LOGICAL_URL_PREFIX)
		  || url.equals(FhirURLConstants.SNOMED_ID)
		  || url.startsWith("http://fhir.nhs.net/Id");
	}
	
	public boolean isLogicalUrl() {
		return isLogicalUrl(toFullString());
	}
}
