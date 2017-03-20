package uk.nhs.fhir.util;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Content;
import org.jdom2.Document;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.CSSStyleBlock;
import uk.nhs.fhir.makehtml.HTMLDocSection;

public class SectionedHTMLDoc extends HTMLDocSection {
	
	public void addSection(HTMLDocSection section) {
		styles.addAll(section.getStyles());
		headElements.addAll(section.getHeadElements());
		bodyElements.addAll(section.getBodyElements());
	}
	
	public Document getHTML() throws ParserConfigurationException {
		List<Content> head = Lists.newArrayList();
		headElements.forEach((Content c) -> {head.add(c.clone());});
		
		List<String> formattedStyleBlocks = Lists.newArrayList();
		styles.forEach((CSSStyleBlock block) -> formattedStyleBlocks.add(block.toFormattedString()));
		
		headElements.add(Elements.withText("style", "\n" + String.join("\n", formattedStyleBlocks) + "\n"));
		
		return new Document(Elements.withChildren("html",
			Lists.newArrayList(
				Elements.withChildren("head", headElements),
				Elements.withChildren("body", bodyElements))));
	}
}