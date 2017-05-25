package uk.nhs.fhir.makehtml.structdef;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.NotImplementedException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.ContactPointDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition.Contact;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition.Mapping;
import ca.uhn.fhir.model.primitive.StringDt;
import uk.nhs.fhir.makehtml.HTMLConstants;
import uk.nhs.fhir.makehtml.HTMLDocSection;
import uk.nhs.fhir.makehtml.html.FhirPanel;
import uk.nhs.fhir.makehtml.html.MetadataTableFormatter;
import uk.nhs.fhir.makehtml.html.Table;
import uk.nhs.fhir.util.Elements;
import uk.nhs.fhir.util.StringUtil;

public class StructureDefinitionMetadataFormatter extends MetadataTableFormatter {

	@Override
	public HTMLDocSection makeSectionHTML(IBaseResource source) throws ParserConfigurationException {
		StructureDefinition structureDefinition = (StructureDefinition)source;
		HTMLDocSection section = new HTMLDocSection();
		
		Element metadataPanel = getMetadataTable(structureDefinition);
		section.addBodyElement(metadataPanel);
		
		getStyles().forEach(section::addStyle);
		Table.getStyles().forEach(section::addStyle);
		FhirPanel.getStyles().forEach(section::addStyle);
		
		return section;
	}
	
	public Element getMetadataTable(StructureDefinition source) {
		
		// These are all required and so should always be present
		String name = source.getName();
		String url = source.getUrl();
		String kind = source.getKind();
		
		String status = source.getStatus();
		Boolean isAbstract = source.getAbstract();
		String displayIsAbstract = isAbstract ? "Yes" : "No";
		
		Optional<String> constrainedType = Optional.ofNullable(source.getConstrainedType());
		
		Optional<String> displayBaseUrl = Optional.empty();
		String origBaseUrl = source.getBase();
		if (origBaseUrl != null) {
			displayBaseUrl = Optional.of(HTMLConstants.HL7_DSTU2 + origBaseUrl.substring(origBaseUrl.lastIndexOf('/'), origBaseUrl.length()));
		}
		
		Optional<String> version = Optional.ofNullable(source.getVersion());
		Optional<String> display = Optional.ofNullable(source.getDisplay());
		
		String displayExperimental;
		Boolean experimental = source.getExperimental();
		if (experimental == null) {
			displayExperimental = BLANK;
		} else {
			displayExperimental = experimental ? "Yes" : "No";
		}
		
		Optional<String> publisher = Optional.ofNullable(source.getPublisher());
		
		Date date = source.getDate();
		Optional<String> displayDate = 
			(date == null) ?
				Optional.empty() : 
				Optional.of(StringUtil.dateToString(date));
		
		Optional<String> requirements = Optional.ofNullable(source.getRequirements());
		Optional<String> copyrightInfo = Optional.ofNullable(source.getCopyright());
		Optional<String> fhirVersion = Optional.ofNullable(source.getFhirVersion());
		Optional<String> contextType = Optional.ofNullable(source.getContextType());
		
		List<List<Content>> identifierCells = Lists.newArrayList();
		for (IdentifierDt identifier : source.getIdentifier()) {
			List<Content> identifierCell = Lists.newArrayList();
			identifierCells.add(identifierCell);
			
			Optional<String> use = Optional.ofNullable(identifier.getUse());
			Optional<String> type = Optional.ofNullable(identifier.getType().getText());
			Optional<String> system = Optional.ofNullable(identifier.getSystem());
			Optional<String> value = Optional.ofNullable(identifier.getValue());
			Optional<PeriodDt> period = Optional.ofNullable(identifier.getPeriod());
			ResourceReferenceDt assigner = identifier.getAssigner();
			
			throw new NotImplementedException("Identifier");
		}
		
		List<Content> publishingOrgContacts = getPublishingOrgContactsContents(source);
		
		List<String> useContexts = Lists.newArrayList();
		for (CodeableConceptDt useContext : source.getUseContext()) {
			for (CodingDt coding : useContext.getCoding()) {
				// think text should be enough
			}
			
			String text = useContext.getText();
			useContexts.add(text);
		}
		
		List<String> indexingCodes = Lists.newArrayList();
		for (CodingDt code : source.getCode()) {
			indexingCodes.add(code.getCode());
		}
		
		List<Mapping> mappings = source.getMapping();
		List<Content> externalSpecMappings = Lists.newArrayList();
		boolean multipleMappings = mappings.size() >= 2;
		if (multipleMappings) {
			externalSpecMappings.add(0, Elements.withAttributeAndText("span", new Attribute("class", "fhir-metadata-block-title"), "External Specifications"));
		}
		for (Mapping mapping : mappings) {
			// always present
			String identity = mapping.getIdentity();
			
			Optional<String> mappingUri = Optional.ofNullable(mapping.getUri());
			Optional<String> mappingName = Optional.ofNullable(mapping.getName());
			Optional<String> mappingComments = Optional.ofNullable(mapping.getComments());
			
			String displayName = mappingName.orElse(identity);
			
			if (!externalSpecMappings.isEmpty()) {
				externalSpecMappings.add(Elements.newElement("br"));
			}
			
			displayName += ": ";
			
			externalSpecMappings.add(
				Elements.withAttributeAndText("span", new Attribute("class", "fhir-telecom-name"), displayName));
			if (mappingUri.isPresent()) {
				externalSpecMappings.add(Elements.withAttributeAndText("span", new Attribute("class", "fhir-telecom-value"), mappingUri.get()));
			}
			if (mappingComments.isPresent()) {
				externalSpecMappings.add(Elements.withAttributeAndText("span", new Attribute("class", "fhir-telecom-value"), "(" + mappingComments.get() + ")"));
			}
		}
		
		List<String> useLocationContexts = Lists.newArrayList();
		for (StringDt context : source.getContext()) {
			useLocationContexts.add(context.getValue());
		}
		
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

		String gridName = name;
		if (version.isPresent()) {
			gridName += " (v" + version.get() + ")";
		}
		
		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("Name", gridName, 2, true),
				//labelledValueCell("Version", version, 1),
				labelledValueCell("URL", url, 2, true)));
		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("Last updated", displayDate, 1),
				labelledValueCell("Status", status, 1),
				labelledValueCell("Kind", StringUtil.capitaliseLowerCase(kind), 1),
				labelledValueCell("FHIR Version", fhirVersion, 1)));
		tableContent.add(
			Elements.withChildren("tr", 
				labelledValueCell("Constrained type", constrainedType, 1),
				labelledValueCell("Base resource", displayBaseUrl, 1),
				labelledValueCell("Abstract", displayIsAbstract, 1),
				labelledValueCell("Experimental", displayExperimental, 1)));
		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("Published by", publisher, 1), 
				labelledValueCell("DisplayName", display, 1),
				labelledValueCell("Requirements", requirements, 1),
				labelledValueCell("Context type", contextType, 1)));
		
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
		
		if (!indexingCodes.isEmpty()) {
			throw new NotImplementedException("Code");
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
				new Attribute("class", "fhir-table"),
				tableContent);
		
		String panelTitleName = display.isPresent() ? display.get() : name;
		String panelTitle = "Structure definition: " + panelTitleName;
		
		FhirPanel panel = new FhirPanel(panelTitle, table);
		
		return panel.makePanel();
	}

	List<Content> getPublishingOrgContactsContents(StructureDefinition source) {
		List<Content> publishingOrgContacts = Lists.newArrayList();
		for (Contact contact : source.getContact()) {			
			Optional<String> individualName  = Optional.ofNullable(contact.getName());
			List<ContactPointDt> individualTelecoms = contact.getTelecom();
			if (!individualTelecoms.isEmpty()) {
				if (!publishingOrgContacts.isEmpty()) {
					publishingOrgContacts.add(Elements.newElement("br"));
				}
				
				String telecomDesc = individualName.isPresent() ? individualName.get() : "General";
				publishingOrgContacts.add(Elements.withAttributeAndText("span", new Attribute("class", "fhir-telecom-name"), telecomDesc));
				if (individualTelecoms.size() == 1) {
					publishingOrgContacts.add(Elements.withAttributeAndText("span", new Attribute("class", "fhir-telecom-name"), ": "));
					publishingOrgContacts.add(Elements.withAttributeAndText("span", new Attribute("class", "fhir-metadata-value"), individualTelecoms.get(0).getValue()));
				} else {
					for (ContactPointDt individualTelecom : individualTelecoms) {
						publishingOrgContacts.add(Elements.newElement("br"));
						publishingOrgContacts.add(Elements.withAttributeAndText("span", new Attribute("class", "fhir-metadata-value"), "\t" + individualTelecom.getValue()));
					}
				}
			}
		}
		
		if (!publishingOrgContacts.isEmpty()) {
			publishingOrgContacts.add(0, Elements.withAttributeAndText("span", new Attribute("class", "fhir-metadata-label"), "Contacts"));
			publishingOrgContacts.add(1, Elements.newElement("br"));
		}
		
		return publishingOrgContacts;
	}
}
