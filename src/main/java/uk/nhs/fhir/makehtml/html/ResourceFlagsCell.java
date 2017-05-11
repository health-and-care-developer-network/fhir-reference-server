package uk.nhs.fhir.makehtml.html;

import java.util.List;
import java.util.Set;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.CSSStyleBlock;
import uk.nhs.fhir.makehtml.data.ResourceFlag;
import uk.nhs.fhir.makehtml.data.ResourceFlags;
import uk.nhs.fhir.util.Elements;

public class ResourceFlagsCell extends TableCell {

	private final ResourceFlags resourceFlags;

	public ResourceFlagsCell(ResourceFlags resourceFlags) {
		this.resourceFlags = resourceFlags;
	}

	@Override
	public Element makeCell() {
		Set<ResourceFlag> flags = resourceFlags.getFlags();
		
		List<Content> renderedFlags = Lists.newArrayList();

		addIfPresent(ResourceFlag.SUMMARY, flags, renderedFlags);
		addIfPresent(ResourceFlag.MODIFIER, flags, renderedFlags);
		addIfPresent(ResourceFlag.CONSTRAINED, flags, renderedFlags);
		addIfPresent(ResourceFlag.MUSTSUPPORT, flags, renderedFlags);
		addIfPresent(ResourceFlag.NOEXTEND, flags, renderedFlags);
		
		return Elements.withAttributeAndChildren("td", 
			new Attribute("class", "fhir-resource-flag"), 
			renderedFlags);
	}

	private void addIfPresent(ResourceFlag flag, Set<ResourceFlag> flags, List<Content> renderedFlags) {
		if (flags.contains(flag)) {
			renderedFlags.add(
				Elements.withAttributeAndText("span", 
					new Attribute("title", flag.getDesc()), 
					flag.getFlag()));
		}
	}

	public static List<CSSStyleBlock> getStyles() {
		List<CSSStyleBlock> styles = Lists.newArrayList();
		
		/*styles.add(
			new CSSStyleBlock(Lists.newArrayList(".fhir-resource-flag span"),
				Lists.newArrayList(
					new CSSRule("background-color", "#ffffbb"))));*/
		
		return styles;
	}
}
