package uk.nhs.fhir.makehtml.fmt;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Text;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.ResourceFlag;
import uk.nhs.fhir.util.Elements;
import uk.nhs.fhir.util.StringUtil;

public class ValueWithResourceFlagsCell implements TableCell {

	private final String value;
	private final List<ResourceFlag> resourceFlags;
	
	public ValueWithResourceFlagsCell(String value, List<ResourceFlag> resourceFlags) {
		this.value = value;
		this.resourceFlags = resourceFlags;
	}

	@Override
	public Element makeCell() {
		List<Content> valueDataNodes = Lists.newArrayList();
		valueDataNodes.add(new Text(StringUtil.capitaliseLowerCase(value)));
		for (ResourceFlag flag : resourceFlags) {
			valueDataNodes.add(new Element("br"));
			valueDataNodes.addAll(nodesForResourceFlag(flag));
		}
		
		return Elements.withChildren("td", valueDataNodes);
	}
	
	private List<Content> nodesForResourceFlag(ResourceFlag flag) {
		List<Content> nodes = Lists.newArrayList(
			Elements.withAttributeAndText("span", 
				new Attribute("class", "fhir-resource-flag"),
				flag.getName()),
			flag.descriptionIsLink() ?
				Elements.withAttributesAndText("a", 
					Lists.newArrayList(
						new Attribute("href", flag.getDescription()),
						new Attribute("class", "fhir-link")),
					flag.getDescription()) :
				new Text(StringUtil.capitaliseLowerCase(flag.getDescription())));
		
		for (String tag: flag.getExtraTags()) {
			nodes.add(getFormattedTag(tag));
		}
		
		return nodes;
	}
	
	private Content getFormattedTag(String tag) {
		return 
			Elements.withAttributeAndText("span", 
				new Attribute("class", "fhir-resource-tag"),
				tag);
	}
}
