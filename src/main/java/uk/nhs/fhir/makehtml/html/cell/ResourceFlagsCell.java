package uk.nhs.fhir.makehtml.html.cell;

import java.util.List;
import java.util.Set;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.structdef.ResourceFlag;
import uk.nhs.fhir.data.structdef.ResourceFlags;
import uk.nhs.fhir.makehtml.html.jdom2.Elements;
import uk.nhs.fhir.makehtml.html.style.CSSStyleBlock;
import uk.nhs.fhir.makehtml.html.style.FhirCSS;

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
			new Attribute("class", FhirCSS.RESOURCE_FLAG), 
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
			new CSSStyleBlock(Lists.newArrayList("." + FhirCSS.RESOURCE_FLAG + " span"),
				Lists.newArrayList(
					new CSSRule(CSSTag.BACKGROUND_COLOR, FhirColour.RESOURCE_FLAG_HIGHLIGHT))));*/
		
		return styles;
	}
}
