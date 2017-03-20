package uk.nhs.fhir.util;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;

public class TableTitle {
	private final String title;
	private final String hoverText;
	private final String cssWidth;
	
	public TableTitle(String title, String hoverText, String cssWidth) {
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
	
	public Element makeTitleCell() {
		return Elements.withAttributesAndText("th",
				Lists.newArrayList(
					new Attribute("title", hoverText),
					new Attribute("style", "width: " + cssWidth),
					new Attribute("class", "fhir-table-title")), 
				title);
	}
}
