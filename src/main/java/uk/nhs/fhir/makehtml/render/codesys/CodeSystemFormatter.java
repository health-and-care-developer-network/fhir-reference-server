package uk.nhs.fhir.makehtml.render.codesys;

import javax.xml.parsers.ParserConfigurationException;

import uk.nhs.fhir.data.wrap.WrappedCodeSystem;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;

public class CodeSystemFormatter extends ResourceFormatter<WrappedCodeSystem> {

    public CodeSystemFormatter(WrappedCodeSystem wrappedResource) {
		super(wrappedResource);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		
		HTMLDocSection metadataPanel = new CodeSystemMetadataFormatter(wrappedResource).makeSectionHTML();
		HTMLDocSection conceptsPanel = new CodeSystemConceptTableFormatter(wrappedResource).makeSectionHTML();
		HTMLDocSection filterRowsPanel = new CodeSystemFiltersTableFormatter(wrappedResource).makeSectionHTML();
		
		HTMLDocSection codeSystemSection = new HTMLDocSection();
		
		codeSystemSection.addSection(metadataPanel);
		codeSystemSection.addSection(conceptsPanel);
		codeSystemSection.addSection(filterRowsPanel);

		return codeSystemSection;
	}
}
