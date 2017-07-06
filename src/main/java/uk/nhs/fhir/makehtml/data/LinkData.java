package uk.nhs.fhir.makehtml.data;

public interface LinkData {
	public FhirURL getURL();
	public String getText();
	
	public SimpleLinkData getPrimaryLinkData();
}
