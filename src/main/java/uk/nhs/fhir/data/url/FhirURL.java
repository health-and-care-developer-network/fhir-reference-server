package uk.nhs.fhir.data.url;

import java.net.MalformedURLException;
import java.util.Set;

import com.google.common.collect.Sets;

import uk.nhs.fhir.data.FhirURLConstants;
import uk.nhs.fhir.util.FhirVersion;

public abstract class FhirURL {
	
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
				throw new IllegalStateException("Not a valid FHIR URL string");
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
		  || url.equals(FhirURLConstants.SNOMED_ID);
	}
}
