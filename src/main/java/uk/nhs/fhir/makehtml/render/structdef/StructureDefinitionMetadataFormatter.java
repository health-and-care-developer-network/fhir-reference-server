package uk.nhs.fhir.makehtml.render.structdef;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.NotImplementedException;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import uk.nhs.fhir.data.FhirURLConstants;
import uk.nhs.fhir.data.structdef.FhirContact;
import uk.nhs.fhir.data.structdef.FhirContacts;
import uk.nhs.fhir.data.structdef.FhirMapping;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.makehtml.html.jdom2.Elements;
import uk.nhs.fhir.makehtml.html.panel.FhirPanel;
import uk.nhs.fhir.makehtml.html.style.FhirCSS;
import uk.nhs.fhir.makehtml.html.table.MetadataTableFormatter;
import uk.nhs.fhir.makehtml.html.table.Table;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.util.StringUtil;

public class StructureDefinitionMetadataFormatter extends MetadataTableFormatter<WrappedStructureDefinition> {

	public StructureDefinitionMetadataFormatter(WrappedStructureDefinition wrappedResource) {
		super(wrappedResource);
	}

	@Override
	public HTMLDocSection makeSectionHTML(WrappedStructureDefinition structureDefinition) throws ParserConfigurationException {
		HTMLDocSection section = new HTMLDocSection();
		
		Element metadataPanel = getMetadataTable(structureDefinition);
		section.addBodyElement(metadataPanel);
		
		getStyles().forEach(section::addStyle);
		Table.getStyles().forEach(section::addStyle);
		FhirPanel.getStyles().forEach(section::addStyle);
		
		return section;
	}
	
	public Element getMetadataTable(WrappedStructureDefinition structureDefinition) {
		
		// These are all required and so should always be present
		String name = structureDefinition.getName();
		String url = structureDefinition.getUrl();
		String kind = structureDefinition.getKind();
		
		String status = structureDefinition.getStatus();
		Boolean isAbstract = structureDefinition.getAbstract();
		String displayIsAbstract = isAbstract ? "Yes" : "No";
		
		Optional<String> constrainedType = structureDefinition.getConstrainedType();
		
		Optional<String> displayBaseUrl = Optional.empty();
		String origBaseUrl = structureDefinition.getBase();
		if (origBaseUrl != null) {
			if (origBaseUrl.equals("http://hl7.org/fhir/StructureDefinition/Extension")) {
				displayBaseUrl = Optional.of("http://hl7.org/fhir/extensibility.html#extension");
			} else {
				displayBaseUrl = Optional.of(FhirURLConstants.HTTP_HL7_FHIR + origBaseUrl.substring(origBaseUrl.lastIndexOf('/')) + ".html");
			}
		}
		
		// version is kept in a meta tag
		Optional<String> version = structureDefinition.getVersion();
		Optional<String> display = structureDefinition.getDisplay();
		
		// never used in NHS Digital profiles
		/*
		String displayExperimental;
		Boolean experimental = source.getExperimental();
		if (experimental == null) {
			displayExperimental = BLANK;
		} else {
			displayExperimental = experimental ? "Yes" : "No";
		}*/
		
		Optional<String> publisher = structureDefinition.getPublisher();
		
		
		Date date = structureDefinition.getDate();
		Optional<String> displayDate = 
			(date == null) ?
				Optional.empty() : 
				Optional.of(StringUtil.dateToString(date));
		
		Optional<String> copyrightInfo = structureDefinition.getCopyright();
		
		Optional<String> fhirVersionDesc = structureDefinition.getFhirVersion();
		
		Optional<String> contextType = structureDefinition.getContextType();
		
		List<Content> publishingOrgContacts = getPublishingOrgContactsContents(structureDefinition);
		
		List<String> useContexts = structureDefinition.getUseContexts();
		
		List<FhirMapping> mappings = structureDefinition.getMappings();
		List<Content> externalSpecMappings = Lists.newArrayList();
		boolean multipleMappings = mappings.size() >= 2;
		if (multipleMappings) {
			externalSpecMappings.add(0, Elements.withAttributeAndText("span", new Attribute("class", FhirCSS.METADATA_BLOCK_TITLE), "External Specifications"));
		}
		for (FhirMapping mapping : mappings) {
			String displayName = mapping.getName().orElse(mapping.getIdentity());
			
			if (!externalSpecMappings.isEmpty()) {
				externalSpecMappings.add(Elements.newElement("br"));
			}
			
			displayName += ": ";
			
			externalSpecMappings.add(
				Elements.withAttributeAndText("span", new Attribute("class", FhirCSS.TELECOM_NAME), displayName));
			if (mapping.getUri().isPresent()) {
				externalSpecMappings.add(Elements.withAttributeAndText("span", new Attribute("class", FhirCSS.TELECOM_VALUE), mapping.getUri().get()));
			}
			if (mapping.getComments().isPresent()) {
				externalSpecMappings.add(Elements.withAttributeAndText("span", new Attribute("class", FhirCSS.TELECOM_VALUE), "(" + mapping.getComments().get() + ")"));
			}
		}
		
		List<String> useLocationContexts = structureDefinition.getUseLocationContexts();
		
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
				labelledValueCell("URL", url, 2, true)));
		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("Version", StringUtil.firstPresent(structureDefinition.getVersionId(), version), 1),
				labelledValueCell("Constrained type", constrainedType, 1),
				labelledValueCell("Constrained URL", displayBaseUrl, 1),
				labelledValueCell("Status", status, 1)));
		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("Published by", publisher, 1),
				labelledValueCell("Created date", displayDate, 1),
				labelledValueCell("Last updated", structureDefinition.getLastUpdated(), 1),
				labelledValueCell("Kind", StringUtil.capitaliseLowerCase(kind), 1)));
		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("FHIR Version", fhirVersionDesc, 1),
				labelledValueCell("DisplayName", display, 1),
				labelledValueCell("Abstract", displayIsAbstract, 1),
				labelledValueCell("Context type", contextType, 2)));
		
		if (!publishingOrgContacts.isEmpty()) {
			tableContent.add(
				Elements.withChild("tr", 
					cell(publishingOrgContacts, 4)));
		}
		
		if (!externalSpecMappings.isEmpty()) {
			tableContent.add(
				Elements.withChild("tr", 
					cell(externalSpecMappings, 4)));
		}
		
		if (!useContexts.isEmpty()) {
			throw new NotImplementedException("UseContext");
		}
		
		if (!useLocationContexts.isEmpty()) {
			String useLocationContextsDescription = useLocationContexts.size() > 1 ? "Use context" : "Use contexts";  
			
			tableContent.add(
				Elements.withChild("tr", 
						labelledValueCell(useLocationContextsDescription, String.join(", ", useLocationContexts), 4)));
		}
		
		if (copyrightInfo.isPresent()) {
			tableContent.add(
				Elements.withChild("tr", 
					labelledValueCell("", copyrightInfo, 4)));
		}
		
		Element table = 
			Elements.withAttributeAndChildren("table",
				new Attribute("class", FhirCSS.TABLE),
				tableContent);
		
		String panelTitleName = display.isPresent() ? display.get() : name;
		String panelTitle = "Structure definition: " + panelTitleName;
		
		FhirPanel panel = new FhirPanel(panelTitle, table);
		
		return panel.makePanel();
	}

	List<Content> getPublishingOrgContactsContents(WrappedStructureDefinition source) {
		List<Content> publishingOrgContacts = Lists.newArrayList();
		for (FhirContacts contact : source.getContacts()) {
			Optional<String> individualName  = contact.getName();
			List<FhirContact> individualTelecoms = contact.getTelecoms();
			if (!individualTelecoms.isEmpty()) {
				if (!publishingOrgContacts.isEmpty()) {
					publishingOrgContacts.add(Elements.newElement("br"));
				}
				
				String telecomDesc = individualName.isPresent() ? individualName.get() : "General";
				publishingOrgContacts.add(Elements.withAttributeAndText("span", new Attribute("class", FhirCSS.TELECOM_NAME), telecomDesc));
				if (individualTelecoms.size() == 1) {
					publishingOrgContacts.add(Elements.withAttributeAndText("span", new Attribute("class", FhirCSS.TELECOM_NAME), ": "));
					publishingOrgContacts.add(Elements.withAttributeAndText("span", new Attribute("class", FhirCSS.METADATA_VALUE), individualTelecoms.get(0).getContactData()));
				} else {
					List<FhirContact> contactsByPrecedence = 
						individualTelecoms
							.stream()
							// Precedence is more important if number is lower
							// so if precedence is not present, treat it as a very big number (i.e. less important)
							.sorted((contact1, contact2) -> 
							!contact1.getPrecedence().isPresent() && !contact2.getPrecedence().isPresent() ? 0 :
								!contact1.getPrecedence().isPresent() ? 1 : 
								!contact2.getPrecedence().isPresent() ? -1 : 
								Integer.compare(contact1.getPrecedence().get(), contact2.getPrecedence().get()))
							.collect(Collectors.toList()); 
					
					for (FhirContact individualTelecom : contactsByPrecedence) {
						publishingOrgContacts.add(Elements.newElement("br"));
						publishingOrgContacts.add(Elements.withAttributeAndText("span", new Attribute("class", FhirCSS.METADATA_VALUE), "\t" + individualTelecom.getContactData()));
					}
				}
			}
		}
		
		if (!publishingOrgContacts.isEmpty()) {
			publishingOrgContacts.add(0, Elements.withAttributeAndText("span", new Attribute("class", FhirCSS.METADATA_LABEL), "Contacts"));
			publishingOrgContacts.add(1, Elements.newElement("br"));
		}
		
		return publishingOrgContacts;
	}
}
