package uk.nhs.fhir.makehtml.fmt;

import org.jdom2.Element;

import uk.nhs.fhir.util.Elements;

public class SimpleTextCell implements TableCell {

	private final String text;
	
	public SimpleTextCell(String text) {
		this.text = text;
	}
	
	@Override
	public Element makeCell() {
		return Elements.withText("td", text);
	}

}
