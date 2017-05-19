package uk.nhs.fhir.makehtml.html;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Text;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.CSSStyleBlock;
import uk.nhs.fhir.makehtml.data.ResourceInfo;
import uk.nhs.fhir.makehtml.data.ResourceInfoType;
import uk.nhs.fhir.util.Elements;
import uk.nhs.fhir.util.StringUtil;

public class ValueWithInfoCell extends TableCell {

	private final String value;
	private final List<ResourceInfo> resourceInfos;
	
	public ValueWithInfoCell(String value, List<ResourceInfo> constraints) {
		this.value = value;
		this.resourceInfos = constraints;
	}

	@Override
	public Element makeCell() {
		List<Content> valueDataNodes = Lists.newArrayList();
		if (!value.isEmpty()) {
			valueDataNodes.add(new Text(StringUtil.capitaliseLowerCase(value)));
		}
		for (ResourceInfo resourceInfo : resourceInfos) {
			if (!valueDataNodes.isEmpty()) {
				valueDataNodes.add(new Element("br"));
			}
			valueDataNodes.addAll(nodesForResourceFlag(resourceInfo));
		}
		
		return Elements.withAttributeAndChildren("td", new Attribute("class", "fhir-resource-info-cell"), valueDataNodes);
	}
	
	private List<Content> nodesForResourceFlag(ResourceInfo resourceInfo) {
		List<Content> constraintInfoNodes = Lists.newArrayList();
		
		ResourceInfoType type = resourceInfo.getType();
		boolean useTidyStyle = useTidyStyle(type);
		String nameClass = useTidyStyle ?
			"fhir-info-name-bold" :
			"fhir-info-name-block";
		
		// The tidy style doesn't have a box around the title, so needs a colon to separate it from the content
		String name = resourceInfo.getName();
		if (useTidyStyle) {
			name += ": ";
		}
		constraintInfoNodes.add(
			Elements.withAttributeAndText("span", 
				new Attribute("class", nameClass),
				name));
		
		List<Content> resourceInfoText = getResourceInfoContents(resourceInfo);
		
		if (useTidyStyle) {
			constraintInfoNodes.add(
				Elements.withAttributeAndChildren("span",
					new Attribute("class", "fhir-text-italic"),
					resourceInfoText));
		} else {
			constraintInfoNodes.addAll(resourceInfoText);
		}
		
		for (String tag: resourceInfo.getExtraTags()) {
			constraintInfoNodes.add(getFormattedTag(tag));
		}
		
		return constraintInfoNodes;
	}

	List<Content> getResourceInfoContents(ResourceInfo resourceInfo) {
		boolean hasText = resourceInfo.getDescription().isPresent();
		boolean hasLink = resourceInfo.getDescriptionLink().isPresent();
		boolean bracketLink = hasText && hasLink;
		
		if (!hasText && !hasLink) {
			throw new IllegalStateException("Resource info without text or link");
		}
		
		String description = hasText ? resourceInfo.getDescription().get() : "";
		String link = hasLink ? resourceInfo.getDescriptionLink().get().toString() : "";
		
		List<Content> constraintInfoText = Lists.newArrayList();
		if (hasText) {
			constraintInfoText.add(new Text(StringUtil.capitaliseLowerCase(description)));
		}
		
		if (bracketLink) {
			constraintInfoText.add(new Text(" ("));
		}
		
		if (hasLink) {	
			constraintInfoText.add(
				Elements.withAttributesAndText("a", 
					Lists.newArrayList(
						new Attribute("href", link),
						new Attribute("class", "fhir-link")),
					link));
		}
			
		if (bracketLink) {
			constraintInfoText.add(new Text(")"));
		}
		
		return constraintInfoText;
	}
	
	private boolean useTidyStyle(ResourceInfoType type) {
		switch (type) {
		case SLICING:
		case EXTENSION_URL:
		case CONSTRAINT:
			return true;
		default:
			return false;
		}
	}

	private Content getFormattedTag(String tag) {
		return 
			Elements.withAttributeAndText("span", 
				new Attribute("class", "fhir-info-tag-block"),
				tag);
	}
	
	public static List<CSSStyleBlock> getStyles() {
		
		List<CSSStyleBlock> styles = Lists.newArrayList();
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList(".fhir-info-name-block, .fhir-info-tag-block"),
				Lists.newArrayList(
					new CSSRule("display", "inline"),
					new CSSRule("background-color", "#cccccc"),
					new CSSRule("color", "#ffffff"),
					new CSSRule("font-weight", "bold"),
					new CSSRule("font-size", "10px"),
					new CSSRule("padding", ".2em .6em .3em"),
					new CSSRule("text-align", "center"),
					new CSSRule("vertical-align", "baseline"),
					new CSSRule("white-space", "nowrap"),
					new CSSRule("line-height", "2em"),
					new CSSRule("border-radius", ".25em"))));
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList(".fhir-info-name-block"),
				Lists.newArrayList(new CSSRule("background-color", "#cccccc"))));
		
		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList(".fhir-info-tag-block"),
				Lists.newArrayList(new CSSRule("background-color", "#ffbb55"))));
		
		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList(".fhir-info-name-bold"),
				Lists.newArrayList(new CSSRule("font-weight", "bold"))));
		
		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList(".fhir-text-italic"),
				Lists.newArrayList(new CSSRule("font-style", "italic"))));
		
		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList(".fhir-resource-info-cell"),
				Lists.newArrayList(
					new CSSRule("padding", "5px 4px"),
					new CSSRule("border-bottom", "1px solid #F0F0F0"))));
		
		return styles;
	}
}
