package uk.nhs.fhir.render.format.message;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.message.MessageDefinitionFocus;
import uk.nhs.fhir.data.message.MessageResponse;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.LinkData;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.data.wrap.WrappedMessageDefinition;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.TableFormatter;
import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.cell.LinkCell;
import uk.nhs.fhir.render.html.cell.SimpleTextCell;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.style.FhirCSS;
import uk.nhs.fhir.render.html.table.Table;
import uk.nhs.fhir.render.html.table.TableRow;
import uk.nhs.fhir.render.html.table.TableTitle;

public class MessageDefinitionFocusTableFormatter extends TableFormatter<WrappedMessageDefinition> {

	public MessageDefinitionFocusTableFormatter(WrappedMessageDefinition wrappedResource) {
		super(wrappedResource);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		MessageDefinitionFocus focus = wrappedResource.getFocus();
		List<MessageResponse> allowedResponses = wrappedResource.getAllowedResponses();
		
		HTMLDocSection section = new HTMLDocSection();
		Element assetsPanel = buildFocusPanel(focus);
		section.addBodyElement(assetsPanel);
		Element allowedResponsesPanel = buildAllowedResponsesPanel(allowedResponses);
		section.addBodyElement(allowedResponsesPanel);

		addStyles(section);
		return section;
	}

    private Element buildAllowedResponsesPanel(List<MessageResponse> allowedResponses) {

		List<TableRow> responsesRows = Lists.newArrayList();

		for (MessageResponse allowedResponse : allowedResponses) {
			FhirURL allowedResponseResource = allowedResponse.getMessageDefinitionId();
			
			TableRow row = new TableRow();
			if (allowedResponseResource.isLogicalUrl()) {
				row.addCell(new SimpleTextCell(allowedResponseResource.toFullString()));
			} else {
				row.addCell(new LinkCell(new LinkDatas(new LinkData(allowedResponseResource, allowedResponseResource.toFullString()))));
			}
			
			responsesRows.add(row);
		}
		
		Element responsesTable = new Table(getResponsesColumns(), responsesRows).makeTable();
		
		return new FhirPanel("Allowed Responses", responsesTable).makePanel();
	}

	private List<TableTitle> getResponsesColumns() {
		return Lists.newArrayList(new TableTitle("Resource", "Responses to this message", "100%"));
	}

	private Element buildFocusPanel(MessageDefinitionFocus focus) {

		String focusResourceUrl = focus.getStructureDefinitionReference();
		Element focusResourceUrlSpan;
		if (FhirURL.isLogicalUrl(focusResourceUrl)) {
			focusResourceUrlSpan = Elements.withText("span", focusResourceUrl);
		} else {
			focusResourceUrlSpan = Elements.withChild("span", 
				Elements.withAttributesAndText("a", 
					Lists.newArrayList(
						new Attribute("class", FhirCSS.LINK), 
						new Attribute("href", focusResourceUrl)), 
					focusResourceUrl));
		}
		
		List<TableRow> tableRows =
				new MessageDefinitionAssetsTableRowFormatter()
					.formatRows(focus.getAssets(), getResourceVersion());
			
			Element focusTable = new Table(getColumns(), tableRows).makeTable();
		
		Element focusTableWrapper = Elements.withChildren("div", 
			Elements.withAttributeAndChildren("div", 
				new Attribute("class", FhirCSS.DATA_LABEL), 
				Lists.newArrayList(
					Elements.text("Focus Code: " + focus.getCode()),
					Elements.newElement("br"),
					Elements.text("Focus Resource: "),
					focusResourceUrlSpan)),
			Elements.newElement("br"),
			focusTable);
		
		return new FhirPanel("Bundle Assets", focusTableWrapper).makePanel();
	}

	private List<TableTitle> getColumns() {
		return Lists.newArrayList(
			new TableTitle("Asset Type", "Type of FHIR asset (e.g. StructureDefinition)", "20%"),
			new TableTitle("Profile", "Id of the profile which the asset must conform to", "50%"),
			new TableTitle("Profile Version", "Version of the profile which the asset must conform to", "30%"));
	}

	public void addStyles(HTMLDocSection section) {
	    Table.getStyles().forEach(section::addStyle);
	    FhirPanel.getStyles().forEach(section::addStyle);
	    LinkCell.getStyles().forEach(section::addStyle);
    }

}
