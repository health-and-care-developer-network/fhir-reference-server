package uk.nhs.fhir.render.html.panel;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.style.CSSRule;
import uk.nhs.fhir.render.html.style.CSSStyleBlock;
import uk.nhs.fhir.render.html.style.CSSTag;
import uk.nhs.fhir.render.html.style.FhirCSS;
import uk.nhs.fhir.render.html.style.FhirColour;
import uk.nhs.fhir.render.html.style.FhirFont;

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
			new Attribute("class", FhirCSS.PANEL),
			panelContents);
	}

	private boolean hasHeading() {
		return headingText != null && !headingText.isEmpty();
	}
	
	private Element makeHeadingBox() {
		return Elements.withAttributeAndChild("div", 
					new Attribute("class", FhirCSS.PANEL_HEADING_BOX), 
					Elements.withAttributeAndText("h3",
						new Attribute("class", FhirCSS.PANEL_HEADING_TEXT), 
						headingText));
	}

	private Element makeBody() {
		return Elements.withAttributeAndChild("div",
					new Attribute("class", FhirCSS.PANEL_BODY),
					contents);
	}
	
	public static List<CSSStyleBlock> getStyles() {
		List<CSSStyleBlock> styles = Lists.newArrayList();
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList("." + FhirCSS.PANEL), //html
				Lists.newArrayList(
					new CSSRule(CSSTag.FONT_FAMILY, FhirFont.PANEL),
					new CSSRule(CSSTag.FONT_SIZE, "14px"),
					new CSSRule(CSSTag.LINE_HEIGHT, "1.4"),
					new CSSRule(CSSTag.WIDTH, "95%"),
					new CSSRule(CSSTag.MAX_WIDTH, "100%"),
					new CSSRule(CSSTag.WORD_WRAP, "break-word"))));
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList("." + FhirCSS.PANEL), 
				Lists.newArrayList(
					new CSSRule(CSSTag.PADDING, "15px"),
					new CSSRule(CSSTag.MARGIN_BOTTOM, "20"),
					new CSSRule(CSSTag.BACKGROUND_COLOR, FhirColour.PANEL_BACKGROUND),
					new CSSRule(CSSTag.BORDER, "1px solid " + FhirColour.PANEL_BORDER),
					new CSSRule(CSSTag.BORDER_RADIUS, "4px"),
					new CSSRule(CSSTag.BOX_SHADOW, "0 1px 1px rgba(0, 0, 0, 0.05)"))));

		styles.add(
			new CSSStyleBlock(Lists.newArrayList("." + FhirCSS.PANEL + " " + "." + FhirCSS.LINK), 
				Lists.newArrayList(
					new CSSRule(CSSTag.TEXT_DECORATION, "none"),
					new CSSRule(CSSTag.COLOR, FhirColour.LINK))));

		return styles;
	}
}
