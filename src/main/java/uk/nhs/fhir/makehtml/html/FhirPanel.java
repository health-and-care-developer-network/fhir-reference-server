package uk.nhs.fhir.makehtml.html;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.CSSStyleBlock;
import uk.nhs.fhir.util.Elements;

public class FhirPanel {
	
	private final String headingText;
	private final Element contents;
	
	public FhirPanel(Element contents) {
		this.headingText = null;
		this.contents = contents;
	}
	
	public FhirPanel(String headingText, Element contents) {
		this.headingText = headingText;
		this.contents = contents;
	}
	
	public Element makePanel() {
		List<Element> panelContents = Lists.newArrayList();
		
		if (hasHeading()) {
			panelContents.add(makeHeadingBox());
		}
		panelContents.add(makeBody());
		
		return Elements.withAttributeAndChildren("div",
			new Attribute("class", "fhir-panel"),
			panelContents);
	}

	private boolean hasHeading() {
		return headingText != null && !headingText.isEmpty();
	}
	
	private Element makeHeadingBox() {
		return Elements.withAttributeAndChild("div", 
					new Attribute("class", "fhir-panel-heading-box"), 
					Elements.withAttributeAndText("h3",
						new Attribute("class", "fhir-panel-heading-text"), 
						headingText));
	}

	private Element makeBody() {
		return Elements.withAttributeAndChild("div",
					new Attribute("class", "fhir-panel-body"),
					contents);
	}
	
	public static List<CSSStyleBlock> getStyles() {
		List<CSSStyleBlock> styles = Lists.newArrayList();
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList(".fhir-panel"), //html
				Lists.newArrayList(
					new CSSRule("font-family", "Helvetica Neue, Helvetica, Arial, sans-serif"),
					new CSSRule("font-size", "14px"),
					new CSSRule("line-height", "1.4"),
					new CSSRule("width", "95%"),
					new CSSRule("max-width", "100%"),
					new CSSRule("word-wrap", "break-word"))));
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList(".fhir-panel"), 
				Lists.newArrayList(
					new CSSRule("padding", "15px"),
					new CSSRule("margin-bottom", "20"),
					new CSSRule("background-color", "#ffffff"),
					new CSSRule("border", "1px solid #dddddd"),
					new CSSRule("border-radius", "4px"),
					new CSSRule("box-shadow", "0 1px 1px rgba(0, 0, 0, 0.05)"))));

		return styles;
	}
}
