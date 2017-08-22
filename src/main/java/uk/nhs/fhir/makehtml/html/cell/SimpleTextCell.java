package uk.nhs.fhir.makehtml.html.cell;

import org.jdom2.Attribute;
import org.jdom2.Element;

import uk.nhs.fhir.makehtml.html.jdom2.Elements;

public class SimpleTextCell extends TableCell {

	private final String text;

	public SimpleTextCell(String text) {
		this(text, false, false, false);
	}
	
	public SimpleTextCell(String text, boolean bordered, boolean faded, boolean strikethrough) {
		super(bordered, faded, strikethrough);
		this.text = text;
	}
	
	@Override
	public Element makeCell() {
		if (cellClasses.isEmpty()) {
			return Elements.withText("td", text);
		} else {
			return Elements.withAttributeAndText("td", 
				new Attribute("class", String.join(" ", cellClasses)),
				text);
		}
	}

}
