package uk.nhs.fhir.makehtml.html;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.CSSStyleBlock;
import uk.nhs.fhir.util.Elements;
import uk.nhs.fhir.util.LinkData;

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
		Element link = 
			Elements.setClasses(
				Elements.withAttributesAndText("a",
					Lists.newArrayList(
						new Attribute("class", "fhir-link"),
						new Attribute("href", linkData.getURL())),
					linkData.getText()),
				linkClasses);
			
		Element cell =
			Elements.setClasses(
				Elements.withChild("td",link),
				cellClasses);
		
		return cell;
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
