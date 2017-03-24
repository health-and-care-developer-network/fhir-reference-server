package uk.nhs.fhir.makehtml.html;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;
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
import uk.nhs.fhir.makehtml.CSSStyleBlock;
import uk.nhs.fhir.makehtml.HTMLConstants;
import uk.nhs.fhir.util.Elements;
import uk.nhs.fhir.util.StringUtil;

public class StructureDefinitionMetadataFormatter {
	
	private static final String BLANK = "";
	
	private final StructureDefinition source;
	
	public StructureDefinitionMetadataFormatter(StructureDefinition source){
		this.source = source;
	}
	
	public Element getMetadataTable() {
		
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

			if (false) {
				StringUtil.printIfPresent("Use", use);
				StringUtil.printIfPresent("Type", type);
				StringUtil.printIfPresent("System", system);
				StringUtil.printIfPresent("Value", value);
				System.out.println("Period: " + StringUtil.periodToString(period.get()));
				System.out.println(assigner.getDisplay().getValue());
			}
			
			//TODO test with example data
			throw new NotImplementedException("Identifier");
		}
		
		List<Content> publishingOrgContacts = getPublishingOrgContactsContents();
		
		for (CodeableConceptDt useContext : source.getUseContext()) {
			// TODO use contexts
		}
		
		for (CodingDt code : source.getCode()) {
			//TODO organisation codes
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
		
		for (StringDt context : source.getContext()) {
			// TODO contexts
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

	List<Content> getPublishingOrgContactsContents() {
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
	
	private Element labelledValueCell(String label, Optional<String> value, int colspan) {
		String displayValue = value.isPresent() ? value.get() : BLANK;
		return labelledValueCell(label, displayValue, colspan);
	}
	
	private Element labelledValueCell(String label, String value, int colspan) {
		return labelledValueCell(label, value, colspan, false);
	}
		
	private Element labelledValueCell(String label, String value, int colspan, boolean alwaysBig) {
		Preconditions.checkNotNull(value, "value data");
		
		List<Element> cellSpans = Lists.newArrayList();
		if (label.length() > 0) {
			cellSpans.add(labelSpan(label, value.isEmpty()));
		}
		if (value.length() > 0) {
			cellSpans.add(valueSpan(value, alwaysBig));
		}
		
		return cell(cellSpans, colspan);
	}
	
	private Element cell(List<? extends Content> content, int colspan) {
		return Elements.withAttributesAndChildren("td", 
			Lists.newArrayList(
				new Attribute("class", "fhir-metadata-cell"),
				new Attribute("colspan", Integer.toString(colspan))),
			content);
	}
	
	private Element labelSpan(String label, boolean valueIsEmpty) {
		String cssClass = "fhir-metadata-label";
		if (valueIsEmpty) {
			cssClass += " fhir-metadata-label-empty";
		}
		
		if (label.length() > 0) {
			label += ": ";
		} else {
			// if the content is entirely empty, the title span somehow swallows the value span
			// so use a zero-width space character.
			label = "&#8203;";
		}
		
		return Elements.withAttributeAndText("span", 
			new Attribute("class", cssClass), 
			label);
	}
	
	private Element valueSpan(String value, boolean alwaysLargeText) {
		boolean url = (value.startsWith("http://") || value.startsWith("https://"));
		boolean largeText = alwaysLargeText || value.length() < 20;
		String fhirMetadataClass = "fhir-metadata-value";
		if (!largeText) fhirMetadataClass += " fhir-metadata-value-smalltext";
		
		if (url) {
			return Elements.withAttributeAndChild("span", 
				new Attribute("class", fhirMetadataClass), 
				Elements.withAttributesAndText("a", 
					Lists.newArrayList(
						new Attribute("class", "fhir-link"), 
						new Attribute("href", value)), 
				value));
			
		} else {
			return Elements.withAttributeAndText("span", 
				new Attribute("class", fhirMetadataClass), 
				value);
		}
	}
	
	public static List<CSSStyleBlock> getStyles() {
		List<CSSStyleBlock> styles = Lists.newArrayList();

		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList(".fhir-metadata-cell"),
				Lists.newArrayList(
					new CSSRule("border", "1px solid #f0f0f0"))));
		styles.add(
				new CSSStyleBlock(
					Lists.newArrayList(".fhir-metadata-label", ".fhir-telecom-name"),
					Lists.newArrayList(
						new CSSRule("color", "#808080"),
						new CSSRule("font-weight", "bold"),
						new CSSRule("font-size", "13"))));
		styles.add(
				new CSSStyleBlock(
					Lists.newArrayList(".fhir-metadata-label-empty"),
					Lists.newArrayList(
						new CSSRule("color", "#D0D0D0"),
						new CSSRule("font-weight", "normal"))));
		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList(".fhir-metadata-value", ".fhir-telecom-value"),
				Lists.newArrayList(
					new CSSRule("color", "#000000"),
					new CSSRule("font-size", "13"))));
		styles.add(
				new CSSStyleBlock(
					Lists.newArrayList(".fhir-metadata-value-smalltext"),
					Lists.newArrayList(
						new CSSRule("font-size", "10"))));
		styles.add(
				new CSSStyleBlock(
					Lists.newArrayList(".fhir-metadata-block-title"),
					Lists.newArrayList(
							new CSSRule("color", "#808080"),
							new CSSRule("font-weight", "bold"),
							new CSSRule("text-decoration", "underline"),
							new CSSRule("font-size", "13"))));
		
		return styles;
	}
}
