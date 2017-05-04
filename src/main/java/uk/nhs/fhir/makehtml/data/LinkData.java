package uk.nhs.fhir.makehtml.data;

public interface LinkData {
	public String getURL();
	public String getText();
	
	public SimpleLinkData getPrimaryLinkData();
}
