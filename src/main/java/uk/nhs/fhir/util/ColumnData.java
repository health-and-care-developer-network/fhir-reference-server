package uk.nhs.fhir.util;

public class ColumnData {
	private final String title;
	private final String hoverText;
	private final String cssWidth;
	
	public ColumnData(String title, String hoverText, String cssWidth) {
		this.title = title;
		this.hoverText = hoverText;
		this.cssWidth = cssWidth;
	}
	
	public String getTitle() {
		return title;
	}
	public String getHoverText() {
		return hoverText;
	}
	public String getCssWidth() {
		return cssWidth;
	}
}
