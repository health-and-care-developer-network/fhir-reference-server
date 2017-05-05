package uk.nhs.fhir.util;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.CSSStyleBlock;
import uk.nhs.fhir.makehtml.HTMLDocSection;

public class SectionedHTMLDoc extends HTMLDocSection {
	
	public void addSection(HTMLDocSection section) {
		styles.addStylesSection(section.getStyles());
		headElements.addAll(section.getHeadElements());
		bodyElements.addAll(section.getBodyElements());
	}
	
	public Document getHTML() throws ParserConfigurationException {
		return new Document(Elements.withChildren("html",
			Lists.newArrayList(
				Elements.withChildren("head", cloneHeadElementsWithStyle()),
				Elements.withChildren("body", cloneBodyElements()))));
	}

	public Element createStyleSection() {
		List<String> formattedStyleBlocks = Lists.newArrayList();
		styles.getBlocks().forEach((CSSStyleBlock block) -> formattedStyleBlocks.add(block.toFormattedString()));
		Element styleSection = Elements.withText("style", "\n" + String.join("\n", formattedStyleBlocks) + "\n");
		return styleSection;
	}
	
	public List<Content> cloneHeadElementsWithStyle() {
		List<Content> head = cloneHeadElements();
		head.add(createStyleSection());
		return head;
	}

	public List<Content> cloneHeadElements() {
		List<Content> head = Lists.newArrayList();
		headElements.forEach((Content c) -> {head.add(c.clone());});
		return head;
	}

	public List<Content> cloneBodyElements() {
		List<Content> body = Lists.newArrayList();
		bodyElements.forEach((Content c) -> {body.add(c.clone());});
		return body;
	}
}