package uk.nhs.fhir.makehtml.data;

import java.net.MalformedURLException;
import java.net.URL;

import uk.nhs.fhir.makehtml.FhirURLConstants;
import uk.nhs.fhir.makehtml.NewMain;

/**
 * Class to wrap a URL allow corrections to URLs used as links, to make them relative.
 */
public class FhirURL {
	private URL url;
	
	public FhirURL(URL url) {
		this.url = url;
	}
	
	public FhirURL(String url) throws MalformedURLException {
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
		
		if (NewMain.FHIR_HL7_ORG_LINKS_LOCAL 
		  && fullUrl.startsWith(FhirURLConstants.FHIR_HL7_ORG_UK)) {
			return fullUrl.substring(FhirURLConstants.FHIR_HL7_ORG_UK.length());
		} else {
			return url.toString();
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
		
		return url.equals(otherFhirUrl.url);
	}
	
	public int hashCode() {
		return url.hashCode();
	}

	public static FhirURL buildOrThrow(String url) {
		try {
			return new FhirURL(url);
		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		}
	}
}
