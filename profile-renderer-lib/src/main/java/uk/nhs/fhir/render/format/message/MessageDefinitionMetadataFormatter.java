package uk.nhs.fhir.render.format.message;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.codesystem.FhirIdentifier;
import uk.nhs.fhir.data.wrap.WrappedMessageDefinition;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.TableFormatter;
import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.style.FhirCSS;
import uk.nhs.fhir.render.html.table.Table;
import uk.nhs.fhir.util.StringUtil;

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
		String url = messageDefinition.getUrl().get();
		Optional<String> title = messageDefinition.getTitle();
		String name = title.orElse(messageDefinition.getName());
		
		Optional<String> version = messageDefinition.getVersion();
		String identifierSystem = BLANK;
		String identifierType = BLANK;
		Optional<FhirIdentifier> identifier = messageDefinition.getIdentifier();
		if (identifier.isPresent()) {
			identifierSystem = identifier.get().getSystem().orElse(BLANK);
			identifierType = identifier.get().getValue().orElse(BLANK);
		}
		String status = messageDefinition.getStatus();
		
		Optional<String> description = messageDefinition.getDescription();
		Date lastUpdated = messageDefinition.getDate();
		Optional<String> category = messageDefinition.getCategory();
		String event = messageDefinition.getEvent();
		String displayLastUpdated = StringUtil.dateToString(lastUpdated);
		
		Optional<String> copyright = messageDefinition.getCopyright();
		
		Element colgroup = getColGroup(4);
		List<Element> tableContent = Lists.newArrayList(colgroup);
		
		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("Name", name, 2, true),
				labelledValueCell("URL", url, 2, true)));
		
		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("Version", version, 1),
				labelledValueCell("Identifier System", identifierSystem, 1),
				labelledValueCell("Identifier", identifierType, 1),
				labelledValueCell("Status", status, 1)));
		
		if (description.isPresent()) {
			tableContent.add(
				Elements.withChildren("tr",
					labelledValueCell("Description", description.get(), 4)));
		}
		
		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("Category", category.orElse(BLANK), 2),
				labelledValueCell("Last updated", displayLastUpdated, 2)));
		
		tableContent.add(
			Elements.withChildren("tr",	labelledValueCell("Event", event, 4)));
		
		
		if (copyright.isPresent()) {
			tableContent.add(
				Elements.withChildren("tr",
					labelledValueCell("Copyright", copyright.get(), 4)));
		}
		
		Element table = 
			Elements.withAttributeAndChildren("table",
				new Attribute("class", FhirCSS.TABLE),
				tableContent);
		
		String panelTitle = "Message definition: " + name;
		FhirPanel panel = new FhirPanel(panelTitle, table);
		
		return panel.makePanel();
	}
}
