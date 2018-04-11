package uk.nhs.fhir.render.format.message;

import javax.xml.parsers.ParserConfigurationException;

import uk.nhs.fhir.data.wrap.WrappedMessageDefinition;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.TableFormatter;

public class MessageDefinitionFocusTableFormatter extends TableFormatter<WrappedMessageDefinition> {

	public MessageDefinitionFocusTableFormatter(WrappedMessageDefinition wrappedResource) {
		super(wrappedResource);
		// TODO Auto-generated constructor stub
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		wrappedResource.getFocus();
		
		// TODO Auto-generated method stub
		return null;
	}

}
