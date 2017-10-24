package uk.nhs.fhir.makehtml.render.codesys;

import javax.xml.parsers.ParserConfigurationException;

import uk.nhs.fhir.data.wrap.WrappedCodeSystem;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.RendererContext;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;

public class CodeSystemFormatter extends ResourceFormatter<WrappedCodeSystem> {

    public CodeSystemFormatter(WrappedCodeSystem wrappedResource, RendererContext context) {
		super(wrappedResource, context);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		
		HTMLDocSection metadataPanel = new CodeSystemMetadataFormatter(wrappedResource, context).makeSectionHTML();
		HTMLDocSection conceptsPanel = new CodeSystemConceptTableFormatter(wrappedResource, context).makeSectionHTML();
		HTMLDocSection filterRowsPanel = new CodeSystemFiltersTableFormatter(wrappedResource, context).makeSectionHTML();
		
		HTMLDocSection codeSystemSection = new HTMLDocSection();
		
		codeSystemSection.addSection(metadataPanel);
		codeSystemSection.addSection(conceptsPanel);
		codeSystemSection.addSection(filterRowsPanel);

		return codeSystemSection;
	}
}
