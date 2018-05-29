package uk.nhs.fhir.render.html.cell;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.nhs.fhir.data.url.LinkData;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.style.CSSRule;
import uk.nhs.fhir.render.html.style.CSSStyleBlock;
import uk.nhs.fhir.render.html.style.CSSTag;
import uk.nhs.fhir.render.html.style.FhirCSS;
import uk.nhs.fhir.render.html.style.FhirColour;
import uk.nhs.fhir.render.html.tree.FhirIcon;

public class LinkCell extends TableCell {
	private final LinkDatas linkDatas;
	private final Set<String> linkClasses;
	
	private Optional<FhirIcon> linkIconUrl = Optional.empty();

	public LinkCell(LinkDatas linkDatas) {
		this(linkDatas, Sets.newHashSet(), Sets.newHashSet());
	}
	
	public LinkCell(LinkDatas linkDatas, boolean bordered) {
		this(linkDatas, false, false, bordered);
	}
	
	public LinkCell(LinkDatas linkDatas, Set<String> cellClasses, Set<String> linkClasses) {
		this(linkDatas, cellClasses, linkClasses, false, false, false);
	}

	public LinkCell(LinkDatas linkDatas, boolean faded, boolean strikethrough, boolean bordered) {
		this(linkDatas, Sets.newHashSet(), Sets.newHashSet(), faded, strikethrough, bordered);
	}
	
	public LinkCell(LinkDatas linkDatas, Set<String> cellClasses, Set<String> linkClasses, boolean faded, boolean strikethrough, boolean bordered) {
		super(bordered, faded, strikethrough);
		this.linkDatas = linkDatas;
		this.cellClasses.addAll(cellClasses);
		this.linkClasses = linkClasses;
	}

	@Override
	public Element makeCell() {
		if (!linkDatas.isEmpty()) {
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
	private List<Content> makeNestedLinkContents(LinkData primaryLink, List<LinkData> nestedLinks) {
		
		Element outerLink = makeLinkElement(primaryLink);
		
		List<Content> children = Lists.newArrayList(outerLink);
		if (!nestedLinks.isEmpty()
		  // exclude child links for Extensions (these should be displayed as URLs in the ResourceInfos instead
		  && !primaryLink.getText().equals("Extension")) {
			children.add(Elements.text(" ("));
			
			boolean first = true;
			for (LinkData nestedLink : nestedLinks) {
				if (!first) {
					children.add(Elements.text(" | "));
				}
				
				children.add(makeLinkElement(nestedLink));
				
				first = false;
			}
			
			children.add(Elements.text(")"));
		}
		
		if (linkIconUrl.isPresent()) {
			children.add(
    			Elements.withAttributes("img",
           			Lists.newArrayList(
       					new Attribute("src", linkIconUrl.get().getUrl()),
       					new Attribute("class", FhirCSS.TREE_RESOURCE_ICON))));
		}
		
		return children;
	}
	
	private Element makeMultiLinkCell() {
		List<Content> cellContents = Lists.newArrayList();

		for (Map.Entry<LinkData, List<LinkData>> link : linkDatas.links()) {
			if (!cellContents.isEmpty()) {
				cellContents.add(Elements.text(" | "));
			}
			
			LinkData primaryLink = link.getKey();
			if (link.getValue().isEmpty()) {
				Element linkElement = makeLinkElement(primaryLink);

				if (linkIconUrl.isPresent()) {
					linkElement.addContent(
		    			Elements.withAttributes("img",
		           			Lists.newArrayList(
		       					new Attribute("src", linkIconUrl.get().getUrl()),
		       					new Attribute("class", FhirCSS.TREE_RESOURCE_ICON))));
				}
				
				cellContents.add(linkElement);
			} else {
				cellContents.addAll(makeNestedLinkContents(primaryLink, link.getValue()));
			}
		}
		
		return makeDataCell(cellContents);
	}

	private Element makeEmptyCell() {
		return makeDataCell(Lists.newArrayList());
	}
	
	Element makeDataCell(List<Content> children) {
		List<Attribute> attributes = Lists.newArrayList();
		if (colspan.isPresent()) {
			attributes.add(new Attribute("colspan", Integer.toString(colspan.get())));
		}
		
		return Elements.addClasses(
			Elements.withAttributesAndChildren("td",
				attributes,
				children),
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
					new CSSRule(CSSTag.TEXT_DECORATION, "none"),
					new CSSRule(CSSTag.COLOR, FhirColour.LINK))));

		styles.add(
			new CSSStyleBlock(Lists.newArrayList("." + FhirCSS.TEXT_FADED),
				Lists.newArrayList(
					new CSSRule(CSSTag.OPACITY, "0.4"))));
		
		styles.add(
			new CSSStyleBlock(Lists.newArrayList("." + FhirCSS.TEXT_STRIKETHROUGH),
				Lists.newArrayList(
					new CSSRule(CSSTag.TEXT_DECORATION, "line-through"))));
		
		return styles;
	}

	public void setLinkIcon(FhirIcon icon) {
		this.linkIconUrl = Optional.of(icon);
	}
}
