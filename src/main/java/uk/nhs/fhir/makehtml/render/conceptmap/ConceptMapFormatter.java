package uk.nhs.fhir.makehtml.render.conceptmap;

import javax.xml.parsers.ParserConfigurationException;

import uk.nhs.fhir.data.wrap.WrappedConceptMap;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;

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
