package uk.nhs.fhir.render.format.conceptmap;

import java.util.List;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.wrap.WrappedConceptMap;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.TableFormatter;
import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.style.FhirCSS;
import uk.nhs.fhir.render.html.table.Table;

public class ConceptMapMetadataFormatter extends TableFormatter<WrappedConceptMap>{

	public ConceptMapMetadataFormatter(WrappedConceptMap conceptMap) {
		super(conceptMap);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		HTMLDocSection section = new HTMLDocSection();
		
		section.addBodyElement(getMetadataTable());
		
		section.addStyles(getStyles());
		section.addStyles(FhirPanel.getStyles());
		section.addStyles(Table.getStyles());
		
		return section;
	}
	
	public Element getMetadataTable() {
		
		// These are all required and so should always be present
		String name = wrappedResource.getName();

		String status = wrappedResource.getStatus();

		Optional<String> version = wrappedResource.getVersion();

		String sourceDesc = wrappedResource.getSource();
		String targetDesc = wrappedResource.getTarget();
		
		String gridName = name;
		if (version.isPresent()) {
			gridName += " (v" + version.get() + ")";
		}


		Element colgroup = Elements.newElement("colgroup");

		List<Element> tableContent = Lists.newArrayList(colgroup);

		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("Name", gridName, 4, true)));
		tableContent.add(
			Elements.withChildren("tr",
					labelledValueCell("Status", status, 4)));

		tableContent.add(
				Elements.withChildren("tr",
						labelledValueCell("Source", sourceDesc, 4, true)
				));
		tableContent.add(
				Elements.withChildren("tr",
						labelledValueCell("Target", targetDesc, 4, true)
						));



		Element table = 
			Elements.withAttributeAndChildren("table",
				new Attribute("class", FhirCSS.TABLE),
				tableContent);
		
		String panelTitleName = name;
		String panelTitle = "ConceptMap: " + panelTitleName;
		
		FhirPanel panel = new FhirPanel(panelTitle, table);
		
		return panel.makePanel();
	}
}
