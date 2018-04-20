package uk.nhs.fhir.util;

import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

public class DomainTrimmer {
	
	// qualified domains, i.e. with a scheme
	private final Set<String> qdomains;

	public DomainTrimmer(Set<String> qdomains) {
		for (String qdomain : qdomains) {
			if (!qdomain.startsWith(FhirURLConstants.SCHEME_HTTP) 
			  && !qdomain.startsWith(FhirURLConstants.SCHEME_HTTPS)) {
				throw new IllegalStateException("Expected qualified domain to start with " + FhirURLConstants.SCHEME_HTTP + " or " + FhirURLConstants.SCHEME_HTTPS + ": " + qdomain);
			}
		}
		
		this.qdomains = qdomains;
	}
	
	public static DomainTrimmer nhsDomains() {
		return new DomainTrimmer(FhirURLConstants.DEFAULT_LOCAL_QDOMAINS);
	}
	
	public Set<String> getDomainsWithoutScheme() { 
    	return ImmutableSet.copyOf(qdomains.stream()
    		.map(qdomain -> qdomain.substring(qdomain.indexOf("://") + "://".length()))
    		.collect(Collectors.toSet()));
	}
	
	public boolean matchesQDomain(String url) {
		return qdomains.stream().anyMatch(qd -> url.startsWith(qd));
	}

	public boolean matchesLocalDomain(String url) {
		return getDomainsWithoutScheme().stream().anyMatch(domain -> url.startsWith(domain));
	}
	
	public String trimLocalDomainPrefix(String url) {
		for (String domain : getDomainsWithoutScheme()) {
			if (url.startsWith(domain)) {
				return url.substring(domain.length());
			}
		}
		
		throw new IllegalStateException("Expected url (" + url + ") to start with local Domain");
	}
}
