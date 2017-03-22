package uk.nhs.fhir.makehtml.html;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Text;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.CSSStyleBlock;
import uk.nhs.fhir.makehtml.data.LinkData;
import uk.nhs.fhir.makehtml.data.NestedLinkData;
import uk.nhs.fhir.util.Elements;

public class LinkCell implements TableCell {
	private final LinkData linkData;
	private final List<String> cellClasses;
	private final List<String> linkClasses;
	
	public LinkCell(LinkData linkData) {
		this(linkData, Lists.newArrayList(), Lists.newArrayList());
	}
	
	public LinkCell(LinkData linkData, List<String> cellClasses, List<String> linkClasses) {
		this.linkData = linkData;
		this.cellClasses = cellClasses;
		this.linkClasses = linkClasses;
	}
	
	@Override
	public Element makeCell() {
		if (linkData instanceof NestedLinkData) {
			return makeNestedCell();
		} else {
			return makeSimpleCell();
		}
	}

	/**
	 * Outputs format according to how many nested links are present:
	 * <a href="link1">Link 1</a>
	 * <a href="link1">Link 1</a> (<a href="link2">Link 2</a>)
	 * <a href="link1">Link 1</a> (<a href="link2">Link 2</a> | <a href="link3">Link 3</a>)
	 * @return
	 */
	private Element makeNestedCell() {
		if (!(linkData instanceof NestedLinkData)) {
			throw new IllegalStateException("Cannot make a nested cell from a simple LinkData");
		}
		
		NestedLinkData data = (NestedLinkData)linkData;
		Element outerLink = makeLinkElement(data);
		
		List<Content> children = Lists.newArrayList(outerLink);
		List<LinkData> nestedLinks = data.getNestedLinks();
		if (!nestedLinks.isEmpty()) {
			children.add(new Text(" ("));
			
			boolean first = true;
			for (LinkData nestedLink : nestedLinks) {
				if (!first) {
					children.add(new Text(" | "));
				}
				
				children.add(makeLinkElement(nestedLink));
				
				first = false;
			}
			
			children.add(new Text(")"));
		}
		
		Element cell = makeDataCell(children);
		
		return cell;
	}
	
	private Element makeSimpleCell() {
		Element link = makeLinkElement(linkData);
		Element cell = makeDataCell(Lists.newArrayList(link));
		
		return cell;
	}
	
	Element makeDataCell(List<Content> children) {
		return Elements.addClasses(
			Elements.withChildren("td", children),
			cellClasses);
	}

	Element makeLinkElement(LinkData linkData) {
		Element link = 
			Elements.addClasses(
				Elements.withAttributesAndText("a",
					Lists.newArrayList(
						new Attribute("class", "fhir-link"),
						new Attribute("href", linkData.getURL())),
					linkData.getText()),
				linkClasses);
		return link;
	}
	
	public static List<CSSStyleBlock> getStyles() {
		List<CSSStyleBlock> styles = Lists.newArrayList();
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList(".fhir-link"), 
				Lists.newArrayList(
					new CSSRule("text-decoration", "none"),
					new CSSRule("color", "#005EB8"))));

		return styles;
	}
}
