package uk.nhs.fhir.render.format.conceptmap;

import javax.xml.parsers.ParserConfigurationException;

import uk.nhs.fhir.data.wrap.WrappedConceptMap;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.ResourceFormatter;

public class ConceptMapFormatter extends ResourceFormatter<WrappedConceptMap> {

	public ConceptMapFormatter(WrappedConceptMap wrappedResource) {
		super(wrappedResource);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		HTMLDocSection metadataPanel = new ConceptMapMetadataFormatter(wrappedResource).makeSectionHTML();
		HTMLDocSection conceptTable = new ConceptMapTableFormatter(wrappedResource).makeSectionHTML();
		
		HTMLDocSection valueSetSection = new HTMLDocSection();
		
		valueSetSection.addSection(metadataPanel);
		valueSetSection.addSection(conceptTable);

		return valueSetSection;
	}

}
