package uk.nhs.fhir.makehtml.data;

/**
 * Data class holding information for a link to be displayed on a screen
 */
public class LinkData {
	
	private final String url;
	private final String text;
	
	public LinkData(String url, String text) {
		this.url = url;
		this.text = text;
	}
	
	public String getURL() {
		return url;
	}
	public String getText() {
		return text;
	}
}
