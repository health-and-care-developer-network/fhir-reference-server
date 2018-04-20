package uk.nhs.fhir.render.format;

import java.util.List;

import org.jdom2.Content;

import uk.nhs.fhir.render.html.Elements;

public class MetadataListCellRenderer<T> {

	protected void newlineIfNeeded(List<Content> contents) {
		if (!contents.isEmpty()) {
			contents.add(Elements.newElement("br"));
		}
	}

}
