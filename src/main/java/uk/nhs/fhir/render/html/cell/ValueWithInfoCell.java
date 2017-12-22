package uk.nhs.fhir.render.html.cell;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Text;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import uk.nhs.fhir.data.ResourceInfo;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.render.html.jdom2.Elements;
import uk.nhs.fhir.render.html.style.CSSRule;
import uk.nhs.fhir.render.html.style.CSSStyleBlock;
import uk.nhs.fhir.render.html.style.CSSTag;
import uk.nhs.fhir.render.html.style.FhirCSS;
import uk.nhs.fhir.render.html.style.FhirColour;
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
			String valueForDisplay;
			if (value.contains(" | ")) {
				valueForDisplay = value; 
			} else {
				valueForDisplay = StringUtil.capitaliseLowerCase(value); 
			}
			
			valueDataNodes.add(
				Elements.withAttributeAndText("div", 
					new Attribute("class", FhirCSS.INFO_NAME_BOLD), 
					valueForDisplay));
		}
		
		
		
		for (ResourceInfo resourceInfo : resourceInfos) {
			List<Content> resourceInfoContent = Lists.newArrayList();
			resourceInfoContent.add(new Text(getDisplayName(resourceInfo)));
			resourceInfoContent.addAll(getResourceInfoContents(resourceInfo));
			
			valueDataNodes.add(
				Elements.withAttributeAndChildren("div", 
					new Attribute("class", String.join(" ", FhirCSS.INDENT_WRAPPED_TEXT, FhirCSS.INFO_PLAIN)), 
					resourceInfoContent));
		}
		
		return Elements.withAttributeAndChildren("td", 
			new Attribute("class", FhirCSS.RESOURCE_INFO_CELL), 
			valueDataNodes);
	}
	
	private String getDisplayName(ResourceInfo resourceInfo) {
		String name = resourceInfo.getName();
		
		String qualifier = resourceInfo.getQualifier();
		if (!Strings.isNullOrEmpty(qualifier)) {
			name += " " + qualifier.toLowerCase();
		}
		
		name += ": ";
		
		return name;
	}

	List<Content> getResourceInfoContents(ResourceInfo resourceInfo) {
		boolean hasText = resourceInfo.getDescription().isPresent();
		boolean hasLink = resourceInfo.getDescriptionLink().isPresent();
		boolean bracketLink = hasText && hasLink;
		
		if (!hasText && !hasLink) {
			throw new IllegalStateException("Resource info without text or link");
		}
		
		String description = hasText ? resourceInfo.getDescription().get() : "";
		FhirURL fhirURL = hasLink ? resourceInfo.getDescriptionLink().get() : null;
		
		List<Content> constraintInfoText = Lists.newArrayList();
		if (hasText) {
			String displayText;
			if (StringUtil.looksLikeUrl(description)) {
				//don't capitalise
				displayText = description;
			} else {
				displayText = StringUtil.capitaliseLowerCase(description);
			}
			
			constraintInfoText.add(new Text(displayText));
		}
		
		if (bracketLink) {
			constraintInfoText.add(new Text(" ("));
		}
		
		if (hasLink) {
			if (resourceInfo.getTextualLink()) {
				// This URL would result in a broken link - it is only intended to be used as an identifier
				constraintInfoText.add(new Text(fhirURL.toFullString()));
			} else {
				constraintInfoText.add(
					Elements.withAttributesAndText("a", 
						Lists.newArrayList(
							new Attribute("href", fhirURL.toLinkString()),
							new Attribute("class", FhirCSS.LINK)),
						fhirURL.toFullString()));
			}
		}
			
		if (bracketLink) {
			constraintInfoText.add(new Text(")"));
		}
		
		return constraintInfoText;
	}
	
	public static List<CSSStyleBlock> getStyles() {
		
		List<CSSStyleBlock> styles = Lists.newArrayList();
		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList("." + FhirCSS.INFO_NAME_BOLD),
				Lists.newArrayList(
					new CSSRule(CSSTag.FONT_WEIGHT, "bold"),
					new CSSRule(CSSTag.COLOR, FhirColour.DATA_TITLE))));

		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList("." + FhirCSS.TEXT_ITALIC),
				Lists.newArrayList(
					new CSSRule(CSSTag.FONT_STYLE, "italic"),
					new CSSRule(CSSTag.COLOR, FhirColour.DATA_VALUE))));
		
		styles.add(
				new CSSStyleBlock(
					Lists.newArrayList("." + FhirCSS.INFO_PLAIN),
					Lists.newArrayList(new CSSRule(CSSTag.COLOR, FhirColour.DATA_VALUE))));
		
		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList("." + FhirCSS.RESOURCE_INFO_CELL),
				Lists.newArrayList(
					new CSSRule(CSSTag.PADDING, "5px 4px"),
					new CSSRule(CSSTag.BORDER_BOTTOM, "1px solid " + FhirColour.TREE_INFO_DIVIDER))));
		
		// styles hack to cause wrapped text to be indented
		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList("." + FhirCSS.INDENT_WRAPPED_TEXT),
				Lists.newArrayList(
					new CSSRule(CSSTag.MARGIN_LEFT, "3em"),
					new CSSRule(CSSTag.TEXT_INDENT, "-3em"))));
		
		return styles;
	}
}
