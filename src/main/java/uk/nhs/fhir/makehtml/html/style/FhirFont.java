package uk.nhs.fhir.makehtml.html.style;

public interface FhirFont {
	
	// font-family
	public static final String HELVETICA_WITH_ALTERNATIVES = "Helvetica Neue, Helvetica, Arial, sans-serif";
	public static final String VERDANA = "verdana";
	public static final String SANS_SERIF = "sans-serif";
	
	/*
	public static final String PANEL = HELVETICA_WITH_ALTERNATIVES;
	
	public static final String TABLE_HEADER = VERDANA;
	public static final String TABLE_BODY = SANS_SERIF;

	public static final String DETAILS_HEADER = VERDANA;
	public static final String DETAILS_DATA = VERDANA;
	*/
	
	static final String ALL_TEXT = "Helvetica Neue, Helvetica, Arial, sans-serif";

	public static final String PANEL = ALL_TEXT;
	
	public static final String TABLE_HEADER = ALL_TEXT;
	public static final String TABLE_BODY = ALL_TEXT;

	public static final String DETAILS_HEADER = ALL_TEXT;
	public static final String DETAILS_DATA = ALL_TEXT;
	
}
