package uk.nhs.fhir.makehtml.html;

import java.util.Set;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Sets;

import uk.nhs.fhir.util.Elements;

public class SimpleTextCell implements TableCell {

	private final String text;
	private final Set<String> classes = Sets.newHashSet();
	
	public SimpleTextCell(String text) {
		this(text, Sets.newHashSet());
	}
	
	public SimpleTextCell(String text, Set<String> classes) {
		this.text = text;
		this.classes.addAll(classes);
	}
	
	@Override
	public Element makeCell() {
		if (classes.isEmpty()) {
			return Elements.withText("td", text);
		} else {
			return Elements.withAttributeAndText("td", 
				new Attribute("class", String.join(" ", classes)),
				text);
		}
	}

}
