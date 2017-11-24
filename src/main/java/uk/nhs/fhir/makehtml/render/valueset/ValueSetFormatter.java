package uk.nhs.fhir.makehtml.render.valueset;

import javax.xml.parsers.ParserConfigurationException;

import uk.nhs.fhir.data.wrap.WrappedConceptMap;
import uk.nhs.fhir.data.wrap.WrappedValueSet;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.RendererContext;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;
import uk.nhs.fhir.makehtml.render.conceptmap.ConceptMapFormatter;

public class ValueSetFormatter extends ResourceFormatter<WrappedValueSet> {

    public ValueSetFormatter(WrappedValueSet wrappedResource, RendererContext context) {
		super(wrappedResource, context);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {

		HTMLDocSection metadataPanel = new ValueSetMetadataFormatter(wrappedResource, context).makeSectionHTML();
		HTMLDocSection conceptTable = new ValueSetTableFormatter(wrappedResource, context).makeSectionHTML();
		
		HTMLDocSection valueSetSection = new HTMLDocSection();
		
		valueSetSection.addSection(metadataPanel);
		valueSetSection.addSection(conceptTable);
		
		for (WrappedConceptMap conceptMap : wrappedResource.getConceptMaps(context.getFhirFileRegistry())) {
			HTMLDocSection formattedConceptMap = new ConceptMapFormatter(conceptMap, context).makeSectionHTML();
			valueSetSection.addSection(formattedConceptMap);
		}

		return valueSetSection;
	}
}
