package uk.nhs.fhir.makehtml;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.util.Elements;

public abstract class HTMLDocumentPanel {

	protected abstract String getHeadingText();
	protected abstract Element buildPanelContents();

	public Element buildPanel() {
		
		return Elements.withAttributeAndChildren("div",
			new Attribute("class", "fhir-panel"),
			Lists.<Element>newArrayList(
				Elements.withAttributeAndChild("div", 
					new Attribute("class", "fhir-panel-heading-box"), 
					Elements.withAttributeAndText("h3",
							new Attribute("class", "fhir-panel-heading-text"), 
							getHeadingText())),
				Elements.withAttributeAndChild("div",
					new Attribute("class", "fhir-panel-body"),
					buildPanelContents())));
	}
}
