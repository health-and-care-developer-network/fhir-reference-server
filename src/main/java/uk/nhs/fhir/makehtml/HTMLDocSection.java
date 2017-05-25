package uk.nhs.fhir.makehtml;

import java.util.List;

import org.jdom2.Content;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.html.CSSStyleSection;

public class HTMLDocSection {
	protected CSSStyleSection styles = new CSSStyleSection();
	protected List<Content> headElements = Lists.newArrayList();
	protected List<Content> bodyElements = Lists.newArrayList();

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
