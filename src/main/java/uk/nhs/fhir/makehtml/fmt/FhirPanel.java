package uk.nhs.fhir.makehtml.fmt;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.util.Elements;

public class FhirPanel {
	
	private final String headingText;
	private final Element contents;
	
	public FhirPanel(String headingText, Element contents) {
		this.headingText = headingText;
		this.contents = contents;
	}
	
	public Element makePanel() {
		return Elements.withAttributeAndChildren("div",
			new Attribute("class", "fhir-panel"),
			Lists.<Element>newArrayList(
				Elements.withAttributeAndChild("div", 
					new Attribute("class", "fhir-panel-heading-box"), 
					Elements.withAttributeAndText("h3",
						new Attribute("class", "fhir-panel-heading-text"), 
						headingText)),
				Elements.withAttributeAndChild("div",
					new Attribute("class", "fhir-panel-body"),
					contents)));
	}
}
