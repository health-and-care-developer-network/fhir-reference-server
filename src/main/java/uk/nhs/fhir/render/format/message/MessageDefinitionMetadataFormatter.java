package uk.nhs.fhir.render.format.message;

import java.util.List;
import java.util.Optional;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.codesystem.FhirIdentifier;
import uk.nhs.fhir.data.message.MessageResponse;
import uk.nhs.fhir.data.wrap.WrappedMessageDefinition;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.TableFormatter;
import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.style.FhirCSS;
import uk.nhs.fhir.render.html.table.Table;

public class MessageDefinitionMetadataFormatter extends TableFormatter<WrappedMessageDefinition> {
	public MessageDefinitionMetadataFormatter(WrappedMessageDefinition wrappedResource) {
		super(wrappedResource);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		HTMLDocSection section = new HTMLDocSection();
		
		Element metadataPanel = getMetadataTable(wrappedResource);
		section.addBodyElement(metadataPanel);
		
		getStyles().forEach(section::addStyle);
		Table.getStyles().forEach(section::addStyle);
		FhirPanel.getStyles().forEach(section::addStyle);
		
		return section;
	}
	
	private Element getMetadataTable(WrappedMessageDefinition messageDefinition) {
		Optional<String> url = messageDefinition.getUrl();
		String name = messageDefinition.getName();
		Optional<String> title = messageDefinition.getTitle();
		Optional<String> version = messageDefinition.getVersion();
		Optional<FhirIdentifier> identifier = messageDefinition.getIdentifier();
		
		String status = messageDefinition.getStatus();
		Optional<String> description = messageDefinition.getDescription();
		Date lastUpdated = messageDefinition.getDate();
		
		Optional<String> copyright = messageDefinition.getCopyright(); 
		
		String event = messageDefinition.getEvent();
		
		Optional<String> category = messageDefinition.getCategory();
		
		List<MessageResponse> allowedResponses = messageDefinition.getAllowedResponses();
		
		Element colgroup = getColGroup(4);
		List<Element> tableContent = Lists.newArrayList(colgroup);
		
		Element table = 
			Elements.withAttributeAndChildren("table",
				new Attribute("class", FhirCSS.TABLE),
				tableContent);
		
		String panelTitle = "Message definition: " + name;
		FhirPanel panel = new FhirPanel(panelTitle, table);
		
		return panel.makePanel();
	}
}
