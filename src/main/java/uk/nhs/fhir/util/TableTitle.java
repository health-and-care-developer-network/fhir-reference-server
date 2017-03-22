package uk.nhs.fhir.util;

import java.util.Optional;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;

public class TableTitle {
	private final String title;
	private final String hoverText;
	private final String cssWidth;
	private final Optional<String> maxWidth;
	
	public TableTitle(String title, String hoverText, String cssWidth) {
		this(title, hoverText, cssWidth, null);
	}
	
	public TableTitle(String title, String hoverText, String cssWidth, String maxWidth) {
		this.title = title;
		this.hoverText = hoverText;
		this.cssWidth = cssWidth;
		this.maxWidth = Optional.ofNullable(maxWidth);
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
	public Optional<String> getMaxWidth() {
		return maxWidth;
	}
	
	public Element makeTitleCell() {
		String styleValue = "width: " + cssWidth;
		if (maxWidth.isPresent()) {
			styleValue += "; max-width: " + maxWidth.get();
		}
		
		return Elements.withAttributesAndText("th",
				Lists.newArrayList(
					new Attribute("title", hoverText),
					new Attribute("style", styleValue),
					new Attribute("class", "fhir-table-title")), 
				title);
	}
}
