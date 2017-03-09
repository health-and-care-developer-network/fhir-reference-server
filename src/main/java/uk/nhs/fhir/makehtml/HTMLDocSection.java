package uk.nhs.fhir.makehtml;

import java.util.List;

import org.jdom2.Content;

import com.google.common.collect.Lists;

public class HTMLDocSection {
	protected List<CSSStyleBlock> styles = Lists.newArrayList();
	protected List<Content> headElements = Lists.newArrayList();
	protected List<Content> bodyElements = Lists.newArrayList();

	public void addHeadElement(Content headElement) {
		headElements.add(headElement);
	}
	
	public void addBodyElement(Content bodyElement) {
		bodyElements.add(bodyElement);
	}
	
	public void addStyle(CSSStyleBlock style) {
		styles.add(style);
	}
	
	public List<CSSStyleBlock> getStyles() {
		return styles;
	}
	
	public List<Content> getHeadElements() {
		return headElements;
	}
	
	public List<Content> getBodyElements() {
		return bodyElements;
	}
}
