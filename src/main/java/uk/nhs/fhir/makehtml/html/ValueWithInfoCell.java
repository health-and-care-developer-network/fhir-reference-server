package uk.nhs.fhir.makehtml.html;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Text;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.CSSStyleBlock;
import uk.nhs.fhir.makehtml.data.ResourceInfo;
import uk.nhs.fhir.util.Elements;
import uk.nhs.fhir.util.StringUtil;

public class ValueWithInfoCell implements TableCell {

	private final String value;
	private final List<ResourceInfo> constraints;
	
	public ValueWithInfoCell(String value, List<ResourceInfo> constraints) {
		this.value = value;
		this.constraints = constraints;
	}

	@Override
	public Element makeCell() {
		List<Content> valueDataNodes = Lists.newArrayList();
		valueDataNodes.add(new Text(StringUtil.capitaliseLowerCase(value)));
		for (ResourceInfo flag : constraints) {
			valueDataNodes.add(new Element("br"));
			valueDataNodes.addAll(nodesForResourceFlag(flag));
		}
		
		return Elements.withChildren("td", valueDataNodes);
	}
	
	private List<Content> nodesForResourceFlag(ResourceInfo constraint) {
		List<Content> constraintInfoNodes = Lists.newArrayList();
		
		constraintInfoNodes.add(
			Elements.withAttributeAndText("span", 
				new Attribute("class", "fhir-info-name"),
				constraint.getName()));
		
		boolean hasText = constraint.getDescription().isPresent();
		boolean hasLink = constraint.getDescriptionLink().isPresent();
		
		if (hasText && hasLink) {
			String constraintDescription = constraint.getDescription().get();
			String constraintLink = constraint.getDescriptionLink().get().toString();
			
			// To produce: some info (<a href="http://my_url" class="fhir-link">http://my_url</a>)
			
			constraintInfoNodes.add(new Text(constraintDescription + " ("));
			
			constraintInfoNodes.add(
				Elements.withAttributesAndText("a", 
					Lists.newArrayList(
						new Attribute("href", constraintLink),
						new Attribute("class", "fhir-link")),
					constraintLink));
			
			constraintInfoNodes.add(new Text(")"));
			
		} else if (hasText) {
			String constraintDescription = constraint.getDescription().get();
			
			constraintInfoNodes.add(new Text(StringUtil.capitaliseLowerCase(constraintDescription)));
			
		} else if (hasLink) {
			String constraintLink = constraint.getDescriptionLink().get().toString();
			
			constraintInfoNodes.add(
				Elements.withAttributesAndText("a", 
						Lists.newArrayList(
							new Attribute("href", constraintLink),
							new Attribute("class", "fhir-link")),
						constraintLink));
			
		} else {
			throw new IllegalStateException("Constraint without text or link");
		}
		
		for (String tag: constraint.getExtraTags()) {
			constraintInfoNodes.add(getFormattedTag(tag));
		}
		
		return constraintInfoNodes;
	}
	
	private Content getFormattedTag(String tag) {
		return 
			Elements.withAttributeAndText("span", 
				new Attribute("class", "fhir-info-tag"),
				tag);
	}
	
	public static List<CSSStyleBlock> getStyles() {
		
		List<CSSStyleBlock> styles = Lists.newArrayList();
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList(".fhir-info-name, .fhir-info-tag"),
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
			new CSSStyleBlock(Lists.newArrayList(".fhir-info-name"),
				Lists.newArrayList(new CSSRule("background-color", "#cccccc"))));
		
		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList(".fhir-info-tag"),
				Lists.newArrayList(new CSSRule("background-color", "#ffbb55"))));
		
		return styles;
	}
}
