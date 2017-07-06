package uk.nhs.fhir.makehtml.html;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Text;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.nhs.fhir.makehtml.data.LinkData;
import uk.nhs.fhir.makehtml.data.NestedLinkData;
import uk.nhs.fhir.makehtml.data.SimpleLinkData;
import uk.nhs.fhir.makehtml.html.jdom2.Elements;
import uk.nhs.fhir.makehtml.html.style.CSSRule;
import uk.nhs.fhir.makehtml.html.style.CSSStyleBlock;

public class LinkCell extends TableCell {
	private final List<LinkData> linkDatas;
	private final Set<String> cellClasses;
	private final Set<String> linkClasses;

	public LinkCell(LinkData linkData) {
		this(linkData, Sets.newHashSet(), Sets.newHashSet());
	}
	
	public LinkCell(List<LinkData> linkData) {
		this(linkData, false);
	}
	
	public LinkCell(List<LinkData> typeLinks, boolean faded) {
		this(typeLinks, faded, false);
	}
	
	public LinkCell(LinkData linkData, Set<String> cellClasses, Set<String> linkClasses) {
		this(Lists.newArrayList(linkData), cellClasses, linkClasses, false, false);
	}

	public LinkCell(List<LinkData> typeLinks, boolean faded, boolean strikethrough) {
		this(typeLinks, Sets.newHashSet(), Sets.newHashSet(), faded, strikethrough);
	}
	
	public LinkCell(List<LinkData> linkData, Set<String> cellClasses, Set<String> linkClasses, boolean faded, boolean strikethrough) {
		super(faded, strikethrough);
		this.linkDatas = linkData;
		this.cellClasses = cellClasses;
		this.linkClasses = linkClasses;
	}

	@Override
	public Element makeCell() {
		if (linkDatas.size() > 0) {
			return makeMultiLinkCell();
		} else {
			return makeEmptyCell();
		}
	}

	/**
	 * Outputs format according to how many nested links are present:
	 * <a href="link1">Link 1</a>
	 * <a href="link1">Link 1</a> (<a href="link2">Link 2</a>)
	 * <a href="link1">Link 1</a> (<a href="link2">Link 2</a> | <a href="link3">Link 3</a>)
	 * @return
	 */	
	private List<Content> makeNestedLinkContents(SimpleLinkData primaryLink, List<SimpleLinkData> nestedLinks) {
		
		Element outerLink = makeLinkElement(primaryLink);
		
		List<Content> children = Lists.newArrayList(outerLink);
		if (!nestedLinks.isEmpty()
		  // exclude child links for Extensions (these should be displayed as URLs in the ResourceInfos instead
		  && !primaryLink.getText().equals("Extension")) {
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
		
		return children;
	}
	
	private Element makeMultiLinkCell() {
		List<Content> cellContents = Lists.newArrayList();
		boolean addedLink = false;
		
		// Combine any nested links with the same outer link
		Map<SimpleLinkData, List<SimpleLinkData>> links = new LinkedHashMap<>();
		for (LinkData linkData : linkDatas) {
			SimpleLinkData key = linkData.getPrimaryLinkData();
			
			List<SimpleLinkData> containedLinks;
			if (!links.containsKey(key)) {
				containedLinks = Lists.newArrayList();
				links.put(key, containedLinks);
			} else {
				containedLinks = links.get(key);
			}
			
			if (linkData instanceof NestedLinkData) {
				List<SimpleLinkData> nestedLinks = ((NestedLinkData) linkData).getNestedLinks();
				containedLinks.addAll(nestedLinks);
			}
		}
		
		for (Map.Entry<SimpleLinkData, List<SimpleLinkData>> link : links.entrySet()) {
			if (addedLink) {
				cellContents.add(new Text(" | "));
			}
			
			SimpleLinkData primaryLink = link.getKey();
			if (link.getValue().isEmpty()) {
				cellContents.add(makeLinkElement(primaryLink));
			} else {
				cellContents.addAll(makeNestedLinkContents(primaryLink, link.getValue()));
			}
			addedLink = true;
		}
		
		return makeDataCell(cellContents);
	}

	private Element makeEmptyCell() {
		return makeDataCell(Lists.newArrayList());
	}
	
	Element makeDataCell(List<Content> children) {
		if (getFaded()) {
			cellClasses.add(FhirCSS.TEXT_FADED);
		}
		if (getStrikethrough()) {
			cellClasses.add(FhirCSS.TEXT_STRIKETHROUGH);
		}
		
		return Elements.addClasses(
			Elements.withChildren("td", children),
			cellClasses);
	}

	Element makeLinkElement(LinkData linkData) {
		Element link = 
			Elements.addClasses(
				Elements.withAttributesAndText("a",
					Lists.newArrayList(
						new Attribute("class", FhirCSS.LINK),
						new Attribute("href", linkData.getURL().toLinkString())),
					linkData.getText()),
				linkClasses);
		return link;
	}
	
	public static List<CSSStyleBlock> getStyles() {
		List<CSSStyleBlock> styles = Lists.newArrayList();
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList("." + FhirCSS.LINK), 
				Lists.newArrayList(
					new CSSRule("text-decoration", "none"),
					new CSSRule("color", "#005EB8"))));

		styles.add(
			new CSSStyleBlock(Lists.newArrayList("." + FhirCSS.TEXT_FADED),
				Lists.newArrayList(
					new CSSRule("opacity", "0.4"))));
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList("." + FhirCSS.TEXT_STRIKETHROUGH),
				Lists.newArrayList(
					new CSSRule("text-decoration", "line-through"))));
		
		return styles;
	}
}
