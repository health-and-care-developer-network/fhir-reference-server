package uk.nhs.fhir.makehtml.fmt;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;

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
}
