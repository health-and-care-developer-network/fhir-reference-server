package uk.nhs.fhir.makehtml.html.style;

public interface FhirColour {
	static final String TEAL = "#005eb8";
	static final String DULL_ORANGE = "#ffbb55";
	static final String BRIGHT_YELLOW = "#ffffbb";
	
	static final String WHITE = "#ffffff";
	static final String SUBTLE_GREY = "#f7f7f7";
	static final String QUITE_SUBTLE_GREY = "#efefef";
	static final String VERY_LIGHT_GREY = "#f0f0f0";
	static final String LIGHT_GREY_2 = "#dddddd";
	static final String LIGHT_GREY_3 = "#d0d0d0";
	static final String LIGHT_GREY = "#cccccc";
	static final String SILVER = "#c0c0c0";
	static final String MID_GREY = "#808080";
	static final String BLACK = "#000000";
	
	
	public static final String PANEL_HEADING_BOTTOM = LIGHT_GREY_2;
	public static final String PANEL_HEADING_BACKGROUND = SUBTLE_GREY;
	public static final String PANEL_BORDER = LIGHT_GREY_2;
	public static final String PANEL_BACKGROUND = WHITE;
	
	public static final String TABLE_HEADER_BORDER = VERY_LIGHT_GREY;
	public static final String DATA_CELL_BORDER = VERY_LIGHT_GREY;
	
	public static final String DATA_LABEL = MID_GREY;
	public static final String DATA_LABEL_WITHOUT_VALUE = LIGHT_GREY_3;
	public static final String DATA_VALUE = BLACK;
	
	public static final String MULTILINE_DATA_TITLE = MID_GREY;
	
	public static final String LINK = TEAL;
	
	public static final String TREE_INFO_DIVIDER = VERY_LIGHT_GREY;
	
	public static final String DETAILS_PAGE_DIVIDER = SILVER;
	public static final String DETAILS_HEADER_BACKGROUND = QUITE_SUBTLE_GREY;
	
	public static final String RESOURCE_FLAG_HIGHLIGHT = BRIGHT_YELLOW;
	
	public static final String RESOURCE_INFO_TITLE_BACKGROUND_TEXT = WHITE;
	public static final String RESOURCE_INFO_TITLE_BACKGROUND = LIGHT_GREY;
	public static final String RESOURCE_INFO_ADDITIONAL_BACKGROUND = DULL_ORANGE;
}
