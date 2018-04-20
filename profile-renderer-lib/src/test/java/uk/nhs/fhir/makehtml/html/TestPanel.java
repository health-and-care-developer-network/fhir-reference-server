package uk.nhs.fhir.makehtml.html;

import java.io.IOException;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;
import org.junit.Assert;

import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.HTMLUtil;
import uk.nhs.fhir.render.html.panel.FhirPanel;

public class TestPanel {

	@Test
	public void testSimplePanel() throws IOException {
		Element content = Elements.withAttributeAndText("div", 
			new Attribute("style", "color: #ddddff; background: #5555ff; font-weight: bold"),
			"Test content");
		FhirPanel panel = new FhirPanel("My panel", content);
		Element panelElement = panel.makePanel();
		String panelHTML = HTMLUtil.docToString(new Document(panelElement), false, false);
		String expected = "<div xmlns=\"http://www.w3.org/1999/xhtml\" class=\"fhir-panel\">"
							+ "<div class=\"fhir-panel-heading-box\">"
								+ "<h3 class=\"fhir-panel-heading-text\">My panel</h3>"
							+ "</div>"
							+ "<div class=\"fhir-panel-body\">"
								+ "<div style=\"color: #ddddff; background: #5555ff; font-weight: bold\">Test content</div>"
							+ "</div>"
						+ "</div>";
		Assert.assertEquals(expected, panelHTML);
	}
}
