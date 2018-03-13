package uk.nhs.fhir.render.html.panel;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.style.FhirCSS;

public abstract class HTMLDocumentPanel {

	protected abstract String getHeadingText();
	protected abstract Element buildPanelContents();

	public Element buildPanel() {
		
		return Elements.withAttributeAndChildren("div",
			new Attribute("class", FhirCSS.PANEL),
			Lists.<Element>newArrayList(
				Elements.withAttributeAndChild("div", 
					new Attribute("class", FhirCSS.PANEL_HEADING_BOX), 
					Elements.withAttributeAndText("h3",
							new Attribute("class", FhirCSS.PANEL_HEADING_TEXT), 
							getHeadingText())),
				Elements.withAttributeAndChild("div",
					new Attribute("class", FhirCSS.PANEL_BODY),
					buildPanelContents())));
	}
}
