package uk.nhs.fhir.makehtml.data;

/**
 * Data class holding information for a link to be displayed on a screen
 */
public class SimpleLinkData implements LinkData {
	
	private final String url;
	private final String text;
	
	public SimpleLinkData(String url, String text) {
		this.url = url;
		this.text = text;
	}
	
	public SimpleLinkData getPrimaryLinkData() {
		return this;
	}
	public String getURL() {
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
		
		if (!(other instanceof SimpleLinkData)) {
			return false;
		}
		
		SimpleLinkData otherLink = (SimpleLinkData)other;
		return url.equals(otherLink.getURL())
		  && text.equals(otherLink.getText());
	}
	
	@Override
	public int hashCode() {
		return url.hashCode() + text.hashCode();
	}
}
