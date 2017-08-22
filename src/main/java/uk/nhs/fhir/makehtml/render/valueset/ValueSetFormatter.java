package uk.nhs.fhir.makehtml.render.valueset;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Element;

import uk.nhs.fhir.data.wrap.WrappedConceptMap;
import uk.nhs.fhir.data.wrap.WrappedValueSet;
import uk.nhs.fhir.makehtml.html.cell.LinkCell;
import uk.nhs.fhir.makehtml.html.cell.ResourceFlagsCell;
import uk.nhs.fhir.makehtml.html.cell.ValueWithInfoCell;
import uk.nhs.fhir.makehtml.html.panel.FhirPanel;
import uk.nhs.fhir.makehtml.html.table.Table;
import uk.nhs.fhir.makehtml.html.tree.TablePNGGenerator;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;
import uk.nhs.fhir.makehtml.render.conceptmap.ConceptMapMetadataFormatter;
import uk.nhs.fhir.makehtml.render.conceptmap.ConceptMapTableFormatter;

public class ValueSetFormatter extends ResourceFormatter<WrappedValueSet> {

    public ValueSetFormatter(WrappedValueSet wrappedResource) {
		super(wrappedResource);
	}

	TablePNGGenerator backgrounds = new TablePNGGenerator();

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {

		Element metadataPanel = new ValueSetMetadataFormatter(wrappedResource).getMetadataTable(wrappedResource);
		HTMLDocSection valueSetSection = new HTMLDocSection();
		addStyles(valueSetSection);
		valueSetSection.addBodyElement(metadataPanel);

		// Inline CodeSystem - must have system if present
		// External CodeSystem - should have Import or Include if present

		ValueSetTableFormatter tableformatter = new ValueSetTableFormatter(wrappedResource);
		Element tableDataPanel = tableformatter.getConceptDataTable(wrappedResource);

		valueSetSection.addBodyElement(tableDataPanel);

		// Expansion

		// Included ConceptMaps - this is coded so ConceptMap can be a separate resource
		List<WrappedConceptMap> conceptMaps = wrappedResource.getConceptMaps();
		
		for (WrappedConceptMap conceptMap : conceptMaps) {
			ConceptMapMetadataFormatter conceptMapMetadata = new ConceptMapMetadataFormatter(conceptMap);

			ConceptMapTableFormatter conceptMapTableData = new ConceptMapTableFormatter(conceptMap);
			conceptMapTableData.makeSectionHTML();

			Element conceptMapMetadataPanel = conceptMapMetadata.getMetadataTable();

			addStyles(valueSetSection);
			valueSetSection.addBodyElement(conceptMapMetadataPanel);

            Element containedTableDataPanel = conceptMapTableData.getElementMapsDataTable();

            valueSetSection.addBodyElement(containedTableDataPanel);
		}

		return valueSetSection;
	}

	public void addStyles(HTMLDocSection section) {
		Table.getStyles().forEach(section::addStyle);
		FhirPanel.getStyles().forEach(section::addStyle);
		ValueWithInfoCell.getStyles().forEach(section::addStyle);
		LinkCell.getStyles().forEach(section::addStyle);
		ResourceFlagsCell.getStyles().forEach(section::addStyle);
		ValueSetMetadataFormatter.getStyles().forEach(section::addStyle);
	}
}
