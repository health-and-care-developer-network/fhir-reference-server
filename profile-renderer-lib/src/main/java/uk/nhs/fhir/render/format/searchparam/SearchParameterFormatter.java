package uk.nhs.fhir.render.format.searchparam;

import javax.xml.parsers.ParserConfigurationException;

import uk.nhs.fhir.data.wrap.WrappedSearchParameter;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.ResourceFormatter;

public class SearchParameterFormatter extends ResourceFormatter<WrappedSearchParameter> {

	public SearchParameterFormatter(WrappedSearchParameter wrappedResource) {
		super(wrappedResource);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		HTMLDocSection metadataPanel = new SearchParameterMetadataFormatter(wrappedResource).makeSectionHTML();
		HTMLDocSection tablePanel = new SearchParameterTableFormatter(wrappedResource).makeSectionHTML();
		
		HTMLDocSection searchParameterSection = new HTMLDocSection();
		
		searchParameterSection.addSection(metadataPanel);
		searchParameterSection.addSection(tablePanel);
		
		return searchParameterSection;
	}
	
}
