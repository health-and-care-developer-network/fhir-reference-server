package uk.nhs.fhir.render.format.searchparam;

import java.util.List;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.structdef.FhirContacts;
import uk.nhs.fhir.data.wrap.WrappedSearchParameter;
import uk.nhs.fhir.render.format.FhirContactRenderer;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.TableFormatter;
import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.style.FhirCSS;
import uk.nhs.fhir.render.html.table.Table;
import uk.nhs.fhir.util.StringUtil;

public class SearchParameterMetadataFormatter extends TableFormatter<WrappedSearchParameter> {

	public SearchParameterMetadataFormatter(WrappedSearchParameter wrappedResource) {
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

	private Element getMetadataTable(WrappedSearchParameter searchParam) {
		String url = searchParam.getUrl().get();
		String name = searchParam.getName();
		Optional<String> version = searchParam.getVersion();
		String status = searchParam.getStatus();
		String description = searchParam.getDescription();
		Optional<String> lastUpdated = searchParam.getDate().map(StringUtil::dateToString);
		Optional<String> publisher = searchParam.getPublisher();
		Optional<String> purpose = searchParam.getPurpose();
		List<FhirContacts> contacts = searchParam.getContacts();
		
		Element colgroup = getColGroup(4);
		List<Element> tableContent = Lists.newArrayList(colgroup);
		
		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("Name", name, 2, true),
				labelledValueCell("URL", url, 2, true)));
		
		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("Version", version, 1),
				labelledValueCell("Status", status, 1),
				labelledValueCell("Last Updated", lastUpdated, 2)));

		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("Description", description, 4)));
		
		if (purpose.isPresent()) {
			tableContent.add(
				Elements.withChildren("tr",
					labelledValueCell("Purpose", purpose.get(), 4)));
		}
		
		if (!contacts.isEmpty()) {
			List<Content> renderedPublishingOrgContacts = new FhirContactRenderer().getPublishingOrgContactsContents(contacts);
			tableContent.add(
				Elements.withChild("tr", 
					cell(renderedPublishingOrgContacts, 4)));
		}
		
		if (publisher.isPresent()) {
			tableContent.add(
				Elements.withChildren("tr",
					labelledValueCell("Publisher", publisher.get(), 4)));
		}
		
		Element table = 
			Elements.withAttributeAndChildren("table",
				new Attribute("class", FhirCSS.TABLE),
				tableContent);
		
		String panelTitle = "Search paramter: " + name;
		FhirPanel panel = new FhirPanel(panelTitle, table);
		
		return panel.makePanel();
	}

}
