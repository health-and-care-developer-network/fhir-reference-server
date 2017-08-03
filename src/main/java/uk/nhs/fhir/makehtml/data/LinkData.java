package uk.nhs.fhir.makehtml.data;

/**
 * Data class holding information for a link to be displayed on a screen
 */
public class LinkData {
	
	private final FhirURL url;
	private final String text;
	
	public LinkData(FhirURL url, String text) {
		this.url = url;
		this.text = text;
	}
	
	public LinkData getPrimaryLinkData() {
		return this;
	}
	public FhirURL getURL() {
		return url;
	}
	public String getText() {
		return text;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		
		if (!(other instanceof LinkData)) {
			return false;
		}
		
		LinkData otherLink = (LinkData)other;
		return url.equals(otherLink.getURL())
		  && text.equals(otherLink.getText());
	}
	
	@Override
	public int hashCode() {
		return url.hashCode() + text.hashCode();
	}
}
