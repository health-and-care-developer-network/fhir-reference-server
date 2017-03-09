package uk.nhs.fhir.makehtml;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.util.ColumnData;
import uk.nhs.fhir.util.Elements;

public abstract class HTMLDocumentTablePanel extends HTMLDocumentPanel {

	protected abstract List<ColumnData> panelColumnTitles();
	protected abstract Element createTableBody();
	
	@Override
	protected Element buildPanelContents() {
		return Elements.withChildren("table", 
			Lists.newArrayList(
				createTableHeader(),
				createTableBody()));
	}

	private Element createTableHeader() {
		List<Element> titleElements = Lists.newArrayList();
		for (ColumnData title : panelColumnTitles()) {
			titleElements.add(
				Elements.withAttributesAndText("th", 
					Lists.newArrayList(
						new Attribute("title", title.getHoverText()),
						new Attribute("style", "width: " + title.getCssWidth())), 
					title.getTitle()));
		}
		
		return Elements.withChild("thead",
			Elements.withChildren("tr", titleElements));
	}
}
