package uk.nhs.fhir.render.format.message;

import javax.xml.parsers.ParserConfigurationException;

import uk.nhs.fhir.data.wrap.WrappedMessageDefinition;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.ResourceFormatter;

public class MessageDefinitionFormatter extends ResourceFormatter<WrappedMessageDefinition> {

	public MessageDefinitionFormatter(WrappedMessageDefinition wrappedResource) {
		super(wrappedResource);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		HTMLDocSection metadataPanel = new MessageDefinitionMetadataFormatter(wrappedResource).makeSectionHTML();
		HTMLDocSection focusPanel = new MessageDefinitionFocusTableFormatter(wrappedResource).makeSectionHTML();
		
		HTMLDocSection messageDefinitionSection = new HTMLDocSection();
		
		messageDefinitionSection.addSection(metadataPanel);
		messageDefinitionSection.addSection(focusPanel);
		
		return messageDefinitionSection;
	}

}
