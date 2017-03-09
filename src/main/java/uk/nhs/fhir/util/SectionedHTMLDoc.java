package uk.nhs.fhir.util;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Document;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.HTMLDocSection;

public class SectionedHTMLDoc extends HTMLDocSection {
	
	public void addSection(HTMLDocSection section) {
		styles.addAll(section.getStyles());
		headElements.addAll(section.getHeadElements());
		bodyElements.addAll(section.getBodyElements());
	}
	
	public Document getHTML() throws ParserConfigurationException {
		return new Document(Elements.withChildren("html",
			Lists.newArrayList(
				Elements.withChildren("head", headElements),
				Elements.withChildren("body", bodyElements))));
	}
}
