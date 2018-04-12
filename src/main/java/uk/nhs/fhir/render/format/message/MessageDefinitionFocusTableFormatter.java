package uk.nhs.fhir.render.format.message;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Text;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.message.MessageDefinitionFocus;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.wrap.WrappedMessageDefinition;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.TableFormatter;
import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.cell.LinkCell;
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
		
		HTMLDocSection section = new HTMLDocSection();
		Element assetsPanel = buildFocusPanel(focus);
		section.addBodyElement(assetsPanel);

		addStyles(section);
		return section;
	}

    private Element buildFocusPanel(MessageDefinitionFocus focus) {
		String bundleExtensionUrl = focus.getBundleExtensionUrl();
		// String focusProfileId = focus.getProfileId();
		
		List<TableRow> tableRows =
			new MessageDefinitionAssetsTableRowFormatter()
				.formatRows(focus.getAssets(), getResourceVersion());
		
		Element focusTable = new Table(getColumns(), tableRows).makeTable();
		
		Element bundleUrlSpan;
		if (FhirURL.isLogicalUrl(bundleExtensionUrl)) {
			bundleUrlSpan = Elements.withText("span", bundleExtensionUrl);
		} else {
			bundleUrlSpan = Elements.withChild("span", 
				Elements.withAttributesAndText("a", 
					Lists.newArrayList(
						new Attribute("class", FhirCSS.LINK), 
						new Attribute("href", bundleExtensionUrl)), 
					bundleExtensionUrl));
		}
		
		Element focusTableWrapper = Elements.withChildren("div", 
			Elements.withAttributeAndChildren("div", 
				new Attribute("class", FhirCSS.DATA_LABEL), 
				Lists.newArrayList(
					new Text("Content conforms to: "),
					bundleUrlSpan)),
			Elements.newElement("br"),
			focusTable);
		return new FhirPanel("Bindings", focusTableWrapper).makePanel();
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
