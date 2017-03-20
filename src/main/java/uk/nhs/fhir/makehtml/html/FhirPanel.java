package uk.nhs.fhir.makehtml.html;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;

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
}
