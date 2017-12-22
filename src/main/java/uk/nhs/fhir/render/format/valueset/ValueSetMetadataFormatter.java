package uk.nhs.fhir.render.format.valueset;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import uk.nhs.fhir.data.codesystem.FhirIdentifier;
import uk.nhs.fhir.data.structdef.FhirContacts;
import uk.nhs.fhir.data.wrap.WrappedValueSet;
import uk.nhs.fhir.render.format.FhirContactRenderer;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.TableFormatter;
import uk.nhs.fhir.render.html.cell.LinkCell;
import uk.nhs.fhir.render.html.cell.TableCell;
import uk.nhs.fhir.render.html.jdom2.Elements;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.style.FhirCSS;
import uk.nhs.fhir.render.html.table.Table;
import uk.nhs.fhir.util.StringUtil;

public class ValueSetMetadataFormatter extends TableFormatter<WrappedValueSet> {

	public ValueSetMetadataFormatter(WrappedValueSet valueSet) {
		super(valueSet);
	}
	
	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		HTMLDocSection section = new HTMLDocSection();
		
		Element metadataPanel = getMetadataTable(wrappedResource);
		section.addBodyElement(metadataPanel);

		Table.getStyles().forEach(section::addStyle);
		FhirPanel.getStyles().forEach(section::addStyle);
		LinkCell.getStyles().forEach(section::addStyle);
		
		return section;
	}

	private Element getMetadataTable(WrappedValueSet source) {
		Optional<String>  url = source.getUrl();
		String  name = source.getName();

		String status = source.getStatus();

		Optional<String> OID = source.getOid();
		Optional<String> reference = source.getReference();

		Optional<String> version = source.getVersion();

		// Never used in NHS Digital value sets
		/*String displayExperimental;
		Boolean experimental = source.getExperimental();
		if (experimental == null) {
			displayExperimental = BLANK;
		} else {
			displayExperimental = experimental ? "Yes" : "No";
		}*/
		
		Optional<String> description = source.getDescription();
		Optional<String> publisher = source.getPublisher();
		List<FhirContacts> publishingOrgContacts = source.getContacts();
		Optional<String> copyright = source.getCopyright();

		Optional<String> requirement = source.getRequirements();

		Optional<Date> date = source.getDate();
		Optional<String> displayDate =
			date.isPresent() ?
				Optional.of(StringUtil.dateToString(date.get())) :
				Optional.empty();
        if (!displayDate.isPresent())
        {
            Date lastUpdated = source.getSourceMeta().getLastUpdated();
            if (lastUpdated != null)
            displayDate = Optional.of(StringUtil.dateToString(lastUpdated));
        }
        
        Optional<Boolean> isExperimental = source.getExperimental();
        String experimental = isExperimental.isPresent() ? isExperimental.get().toString() : BLANK;
        
        String identifierSystem = BLANK;
		String identifierType = BLANK;
		List<FhirIdentifier> identifiers = source.getIdentifiers();
		if (identifiers.size() > 1) {
			throw new IllegalStateException("Display multiple ValueSet identifiers");
		}
		for (FhirIdentifier identifier : identifiers) {
			identifierSystem = identifier.getSystem().orElse(BLANK);
			identifierType = identifier.getValue().orElse(BLANK);
		}
		
		Optional<Boolean> isImmutable = source.getImmutable();
		String immutableDesc = isImmutable.isPresent() ? isImmutable.get().toString() : BLANK;

        Element colgroup = Elements.newElement("colgroup");
        int columns = 4;
        Preconditions.checkState(100 % columns == 0, "Table column count divides 100% evenly");

        int percentPerColumn = 100/columns;
        for (int i=0; i<columns; i++) {
            colgroup.addContent(
                    Elements.withAttributes("col",
                            Lists.newArrayList(
                                    new Attribute("width", Integer.toString(percentPerColumn) + "%"))));
        }

        List<Element> tableContent = Lists.newArrayList(colgroup);

		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("Name", name, 2, true),
				labelledValueCell("URL", url.get(), 2, true)));
		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("Version", version, 1),
				labelledValueCell("Status", status, 1),
				labelledValueCell("Last updated", displayDate, 1),
				labelledValueCell("Experimental", experimental, 1)));

		if (!Strings.isNullOrEmpty(identifierSystem)
		  || !Strings.isNullOrEmpty(identifierType)
		  || !immutableDesc.trim().isEmpty()) {
			tableContent.add(
				Elements.withChildren("tr",
					labelledValueCell("Identifier system", identifierSystem, 1, true),
					labelledValueCell("Identifier", identifierType, 1, true),
					labelledValueCell("Immutable", identifierSystem, 1, true),
					TableCell.emptyBordered().makeCell()));
		}
		
		if (description.isPresent()) {
			tableContent.add(
				Elements.withChildren("tr",
					labelledValueCell("Description", description.get(), 4, true)));
		}
        // KGM 5/May/2017
        if (requirement.isPresent()) {
            tableContent.add(
                Elements.withChildren("tr",
                    labelledValueCell("Requirements", requirement.get(), 4, true)));
        }
        if (publisher.isPresent()) {
            tableContent.add(
                Elements.withChildren("tr",
                    labelledValueCell("Publisher", publisher.get(), 4)));
        }
		if (!publishingOrgContacts.isEmpty()) {
			List<Content> renderedPublishingOrgContacts = new FhirContactRenderer().getPublishingOrgContactsContents(publishingOrgContacts);
			tableContent.add(
				Elements.withChild("tr", 
					cell(renderedPublishingOrgContacts, 4)));
		}
        if (copyright.isPresent()) {
            tableContent.add(
                    Elements.withChildren("tr",
                            labelledValueCell("Copyright", copyright.get(), 4, true)));
        }
		if (reference.isPresent()) {
			tableContent.add(
					Elements.withChildren("tr",
							labelledValueCell("Reference", reference.get(), 4, true)));
		}

		// Should this be in the identifier section? Makes sense when linked to Hl7v2 tables
		if (OID.isPresent()) {
			tableContent.add(
					Elements.withChildren("tr",
							labelledValueCell("OID", OID.get(), 4, true)
					));
		}

		Element table = 
			Elements.withAttributeAndChildren("table",
				new Attribute("class", FhirCSS.TABLE),
				tableContent);
		
		String panelTitleName = name;
		String panelTitle = "ValueSet: " + panelTitleName;
		
		FhirPanel panel = new FhirPanel(panelTitle, table);
		
		return panel.makePanel();
	}
}
