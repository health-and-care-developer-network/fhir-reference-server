package uk.nhs.fhir.render.html.cell;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.render.html.Elements;

public class SimpleTextCell extends TableCell {
	
	private final String text;

	public SimpleTextCell(String text) {
		this(text, false);
	}
	
	public SimpleTextCell(String text, boolean bordered) {
		this(text, bordered, false, false);
	}
	
	public SimpleTextCell(String text, boolean bordered, boolean faded, boolean strikethrough) {
		super(bordered, faded, strikethrough);
		this.text = text;
	}
	
	@Override
	public Element makeCell() {
		List<Attribute> attributes = Lists.newArrayList();
		if (colspan.isPresent()) {
			attributes.add(new Attribute("colspan", Integer.toString(colspan.get())));
		}
		
		return Elements.addClasses(
			Elements.withAttributesAndText("td",
				attributes,
				text),
			cellClasses);
	}

}
