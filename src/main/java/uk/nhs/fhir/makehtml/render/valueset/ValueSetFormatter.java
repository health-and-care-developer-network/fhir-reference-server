package uk.nhs.fhir.makehtml.render.valueset;

import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Element;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.ConceptMap;
import uk.nhs.fhir.makehtml.data.wrap.WrappedResource;
import uk.nhs.fhir.makehtml.data.wrap.WrappedValueSet;
import uk.nhs.fhir.makehtml.html.FhirPanel;
import uk.nhs.fhir.makehtml.html.LinkCell;
import uk.nhs.fhir.makehtml.html.ResourceFlagsCell;
import uk.nhs.fhir.makehtml.html.Table;
import uk.nhs.fhir.makehtml.html.TablePNGGenerator;
import uk.nhs.fhir.makehtml.html.ValueWithInfoCell;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;
import uk.nhs.fhir.makehtml.render.conceptmap.ConceptMapMetadataFormatter;
import uk.nhs.fhir.makehtml.render.conceptmap.ConceptMapTableFormatter;

public class ValueSetFormatter extends ResourceFormatter<WrappedValueSet> {

    public ValueSetFormatter(WrappedValueSet wrappedResource) {
		super(wrappedResource);
	}

	private static final Logger LOG = Logger.getLogger(ValueSetFormatter.class.getName());

	TablePNGGenerator backgrounds = new TablePNGGenerator();

	@Override
	public HTMLDocSection makeSectionHTML(WrappedValueSet valueSet) throws ParserConfigurationException {

		Element metadataPanel = new ValueSetMetadataFormatter().getMetadataTable(valueSet);
		HTMLDocSection section = new HTMLDocSection();
		addStyles(section);
		section.addBodyElement(metadataPanel);

		// Inline CodeSystem - must have system if present
		// External CodeSystem - should have Import or Include if present

		ValueSetTableFormatter tableformatter = new ValueSetTableFormatter();
		Element tableDataPanel = tableformatter.getConceptDataTable(valueSet);

		section.addBodyElement(tableDataPanel);

		// Expansion

		// Included ConceptMaps - this is coded so ConceptMap can be a separate resource
		if (valueSet.getContained().getContainedResources().size() > 0 )
		{
			for (IResource resource :valueSet.getContained().getContainedResources())
			{
				if (resource instanceof ConceptMap)
				{
					ConceptMap conceptMap = (ConceptMap) resource;
					ConceptMapMetadataFormatter conceptMapMetadata = new ConceptMapMetadataFormatter(conceptMap);

					ConceptMapTableFormatter conceptMapTableData = new ConceptMapTableFormatter(conceptMap);

					Element conceptMapMetadataPanel = conceptMapMetadata.getMetadataTable();

					addStyles(section);
					section.addBodyElement(conceptMapMetadataPanel);

                    Element containedTableDataPanel = conceptMapTableData.getElementMapsDataTable();

                    section.addBodyElement(containedTableDataPanel);
				}
			}
		}

		return section;
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
