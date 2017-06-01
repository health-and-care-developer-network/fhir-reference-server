package uk.nhs.fhir.makehtml.html;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Text;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.data.FhirIcon;
import uk.nhs.fhir.util.Elements;

public class TreeNodeCell extends TableCell {

	private final List<FhirTreeIcon> treeIcons;
	private final FhirIcon fhirIcon;
	private final String name;
	private final String backgroundClass;
	private final boolean strikethrough;
	private final String nodeKey;
	
	public TreeNodeCell(List<FhirTreeIcon> treeIcons, FhirIcon fhirIcon, String name, String backgroundClass, boolean strikethrough, String nodeKey) {
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(fhirIcon);
		Preconditions.checkNotNull(treeIcons);
		Preconditions.checkNotNull(backgroundClass);
		Preconditions.checkNotNull(nodeKey);
		
		this.name = name;
		this.fhirIcon = fhirIcon;
		this.treeIcons = treeIcons;
		this.backgroundClass = backgroundClass;
		this.strikethrough = strikethrough;
		this.nodeKey = nodeKey;
	}
	
	@Override
	public Element makeCell() {
		List<Content> contents = Lists.newArrayList();
		for (FhirTreeIcon icon : treeIcons) {
			//contents.add(Elements.withAttributes("img", Lists.newArrayList(new Attribute("class", icon.getCssClass()))));
			contents.add(
				Elements.withAttributes("img", 
					Lists.newArrayList(
						new Attribute("src", icon.getNhsSrc()),
						new Attribute("class", "fhir-tree-icon"))));
		}
		contents.add(Elements.withAttributes("img", 
			Lists.newArrayList(
				new Attribute("src", fhirIcon.getUrl()),
				new Attribute("class", "fhir-tree-resource-icon"))));
		contents.add(
			strikethrough
				? Elements.withAttributeAndText("span", new Attribute("class", "fhir-text-strikethrough"), name)
				: Elements.withAttributesAndChild(
					"a",
					Lists.newArrayList(
						new Attribute("class", String.join(" ", Lists.newArrayList("fhir-link", "tabLink"))),
						new Attribute("href", "details.html#" + nodeKey)), 
					new Text(name)));
		
		return Elements.withAttributeAndChildren("td", 
			new Attribute("class", backgroundClass + " fhir-tree-icons"), 
			contents);
	}

}
