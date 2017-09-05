package uk.nhs.fhir.makehtml.render.valueset;

import javax.xml.parsers.ParserConfigurationException;

import uk.nhs.fhir.data.wrap.WrappedConceptMap;
import uk.nhs.fhir.data.wrap.WrappedValueSet;
import uk.nhs.fhir.makehtml.FhirFileRegistry;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;
import uk.nhs.fhir.makehtml.render.conceptmap.ConceptMapMetadataFormatter;
import uk.nhs.fhir.makehtml.render.conceptmap.ConceptMapTableFormatter;

public class ValueSetFormatter extends ResourceFormatter<WrappedValueSet> {

    public ValueSetFormatter(WrappedValueSet wrappedResource, FhirFileRegistry otherResources) {
		super(wrappedResource, otherResources);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {

		HTMLDocSection metadataPanel = new ValueSetMetadataFormatter(wrappedResource, otherResources).makeSectionHTML();
		HTMLDocSection conceptTable = new ValueSetTableFormatter(wrappedResource, otherResources).makeSectionHTML();
		
		HTMLDocSection valueSetSection = new HTMLDocSection();
		
		valueSetSection.addSection(metadataPanel);
		valueSetSection.addSection(conceptTable);
		
		for (WrappedConceptMap conceptMap : wrappedResource.getConceptMaps()) {
			HTMLDocSection conceptMapMetadata = new ConceptMapMetadataFormatter(conceptMap, otherResources).makeSectionHTML();
			HTMLDocSection conceptMapTableData = new ConceptMapTableFormatter(conceptMap, otherResources).makeSectionHTML();

			valueSetSection.addSection(conceptMapMetadata);
			valueSetSection.addSection(conceptMapTableData);
		}

		return valueSetSection;
	}
}
