package uk.nhs.fhir.render.format.codesys;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.codesystem.FhirIdentifier;
import uk.nhs.fhir.data.structdef.FhirContacts;
import uk.nhs.fhir.data.wrap.WrappedCodeSystem;
import uk.nhs.fhir.render.format.FhirContactRenderer;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.TableFormatter;
import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.cell.LinkCell;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.style.FhirCSS;
import uk.nhs.fhir.render.html.table.Table;
import uk.nhs.fhir.util.StringUtil;

public class CodeSystemMetadataFormatter extends TableFormatter<WrappedCodeSystem> {

	public CodeSystemMetadataFormatter(WrappedCodeSystem wrappedResource) {
		super(wrappedResource);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		HTMLDocSection section = new HTMLDocSection();
		
		Element metadataPanel = getMetadataTable(wrappedResource);
		section.addBodyElement(metadataPanel);
		getStyles().forEach(section::addStyle);

		section.addStyles(TableFormatter.getStyles());
		section.addStyles(Table.getStyles());
		section.addStyles(FhirPanel.getStyles());
		section.addStyles(LinkCell.getStyles());
		
		return section;
	}

	private Element getMetadataTable(WrappedCodeSystem codeSystem) {
		String url = codeSystem.getUrl().get();
		String name = codeSystem.getName();
		Optional<String> title = codeSystem.getTitle();
		
		String version = codeSystem.getVersion().orElse(BLANK);
		String status = codeSystem.getStatus();
		Optional<Boolean> experimental = codeSystem.getExperimental();
		String experimentalDesc = experimental.map(bool -> boolDesc(bool)).orElse(BLANK);
		
		String description = codeSystem.getDescription().orElse(BLANK);
		String purpose = codeSystem.getPurpose().orElse(BLANK);
		Optional<Boolean> caseSensitive = codeSystem.getCaseSensitive();
		String caseSensitiveDesc = caseSensitive.map(bool -> boolDesc(bool)).orElse(BLANK);
		
		Optional<Boolean> compositional = codeSystem.getCompositional();
		String compositionalDesc = compositional.map(bool -> boolDesc(bool)).orElse(BLANK);
		
		String content = codeSystem.getContent().orElse(BLANK);
		
		Optional<Date> lastUpdatedDate = codeSystem.getLastUpdatedDate();
		
		String lastUpdated = 
			codeSystem.getLastUpdated().isPresent() ? 
			codeSystem.getLastUpdated().get() : 
			lastUpdatedDate.isPresent() ?
			StringUtil.dateToString(lastUpdatedDate.get()) :
			BLANK;
		String publisher = codeSystem.getPublisher().orElse(BLANK);
		
		String identifierSystem = BLANK;
		String identifierType = BLANK;
		Optional<FhirIdentifier> identifier = codeSystem.getIdentifier();
		if (identifier.isPresent()) {
			identifierSystem = identifier.get().getSystem().orElse(BLANK);
			identifierType = identifier.get().getValue().orElse(BLANK);
		}
		
		String hierarchyMeaning = codeSystem.getHierarchyMeaning().orElse(BLANK);
		
		List<FhirContacts> publishingOrgContacts = codeSystem.getContacts();
		
		Optional<String> copyright = codeSystem.getCopyright();
		
		Element colgroup = getColGroup(4);
		
		List<Element> tableContent = Lists.newArrayList(colgroup);
		
		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("Name", name, 2, true),
				labelledValueCell("URL", url, 2, true)));
		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("Version", version, 1, true),
				labelledValueCell("Status", status, 1, true),
				labelledValueCell("Purpose", purpose, 2, true)));
		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("Description", description, 4, true)));
		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("Last updated", lastUpdated, 1, true),
				labelledValueCell("Published by", publisher, 1, true),
				labelledValueCell("Identifier system", identifierSystem, 1, true),
				labelledValueCell("Identifier", identifierType, 1, true)));
		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("Compositional", compositionalDesc, 1, true),
				labelledValueCell("Content", content, 1, true),
				labelledValueCell("Experimental", experimentalDesc, 1, true),
				labelledValueCell("Case Sensitive", caseSensitiveDesc, 1, true)));
		
		if (!hierarchyMeaning.isEmpty()) {
			tableContent.add(
				Elements.withChildren("tr",
					labelledValueCell("Hierarchy Meaning", hierarchyMeaning, 4, true)));
		}
		
		if (!publishingOrgContacts.isEmpty()) {
			List<Content> renderedPublishingOrgContacts = new FhirContactRenderer().getPublishingOrgContactsContents(publishingOrgContacts);
			tableContent.add(
				Elements.withChild("tr", 
					cell(renderedPublishingOrgContacts, 4)));
		}
		
		if (copyright.isPresent()) {
			tableContent.add(
				Elements.withChild("tr", 
					labelledValueCell("", copyright, 4)));
		}
		
		Element table = 
			Elements.withAttributeAndChildren("table",
				new Attribute("class", FhirCSS.TABLE),
				tableContent);
		
		String panelTitleName = title.isPresent() ? title.get() : name;
		String panelTitle = "CodeSystem definition: " + panelTitleName;
		
		FhirPanel panel = new FhirPanel(panelTitle, table);
		
		return panel.makePanel();
	}
	
	private static String boolDesc(boolean bool) {
		return bool ? "yes" : "no";
	}
	
}
