package uk.nhs.fhir.render.format;

import java.util.List;

import org.jdom2.Content;

import com.google.common.collect.Lists;

import uk.nhs.fhir.render.html.style.CSSStyleBlock;
import uk.nhs.fhir.render.html.style.CSSStyleSection;

public class HTMLDocSection {
	protected CSSStyleSection styles = new CSSStyleSection();
	protected List<Content> headElements = Lists.newArrayList();
	protected List<Content> bodyElements = Lists.newArrayList();

	public void addSection(HTMLDocSection sectionToAdd) {
		if (sectionToAdd != null) {
			styles.addStylesSection(sectionToAdd.getStyles());
			sectionToAdd.getHeadElements().forEach(headElement -> addHeadElement(headElement));
			sectionToAdd.getBodyElements().forEach(bodyElement -> addBodyElement(bodyElement));
		}
	}

	public void addHeadElement(Content headElement) {
		headElements.add(headElement);
	}
	
	public void addBodyElement(Content bodyElement) {
		bodyElements.add(bodyElement);
	}
	
	public void addStyles(List<CSSStyleBlock> styleBlocks) {
		styles.addStyles(styleBlocks);
	}
	
	public void addStyle(CSSStyleBlock style) {
		styles.addBlock(style);
	}
	
	public CSSStyleSection getStyles() {
		return styles;
	}
	
	public List<Content> getHeadElements() {
		return headElements;
	}
	
	public List<Content> getBodyElements() {
		return bodyElements;
	}
}
