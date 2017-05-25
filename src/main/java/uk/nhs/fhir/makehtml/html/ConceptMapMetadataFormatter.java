package uk.nhs.fhir.makehtml.html;

import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.ConceptMap;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import uk.nhs.fhir.makehtml.CSSStyleBlock;
import uk.nhs.fhir.util.Elements;
import uk.nhs.fhir.util.StringUtil;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class ConceptMapMetadataFormatter {

	private static final String BLANK = "";

	private final ConceptMap source;

	public ConceptMapMetadataFormatter(ConceptMap source){
		this.source = source;
	}
	
	public Element getMetadataTable() {
		
		// These are all required and so should always be present

		Optional<String>  url = Optional.ofNullable(source.getUrl());
		Optional<String>  name = Optional.ofNullable(source.getName());

		String status = source.getStatus().toString();

		Optional<String> OID= Optional.empty();
		Optional<String> reference = Optional.empty();



		Optional<String> version = Optional.ofNullable(source.getVersion());

		String displayExperimental;
		Boolean experimental = source.getExperimental();
		if (experimental == null) {
			displayExperimental = BLANK;
		} else {
			displayExperimental = experimental ? "Yes" : "No";
		}
		Optional<String> description = Optional.ofNullable(source.getDescription());
		Optional<String> publisher = Optional.ofNullable(source.getPublisher());
		Optional<String> copyright = Optional.ofNullable(source.getCopyright());
		//Optional<String> title = Optional.ofNullable(source.getTitle());

		String gridName = name.get();
		if (version.isPresent()) {
			gridName += " (v" + version.get() + ")";
		}

		Date date = source.getDate();
		Optional<String> displayDate =
				(date == null) ?
						Optional.empty() :
						Optional.of(StringUtil.dateToString(date));



		Element colgroup = Elements.newElement("colgroup");
		int columns = 4;

		List<Element> tableContent = Lists.newArrayList(colgroup);

		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("Name", gridName, 4, true)));
		tableContent.add(
			Elements.withChildren("tr",
					labelledValueCell("Status", status, 4)));

		tableContent.add(
				Elements.withChildren("tr",
						labelledValueCell("Source", ((ResourceReferenceDt) source.getSource()).getReference().getValue(), 4, true)
				));
		tableContent.add(
				Elements.withChildren("tr",
						labelledValueCell("Target", ((ResourceReferenceDt) source.getTarget()).getReference().getValue(), 4, true)
						));



		Element table = 
			Elements.withAttributeAndChildren("table",
				new Attribute("class", "fhir-table"),
				tableContent);
		
		String panelTitleName =  name.get();
		String panelTitle = "ConceptMap: " + panelTitleName;
		
		FhirPanel panel = new FhirPanel(panelTitle, table);
		
		return panel.makePanel();
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

	/*
	private String dstu2link(String value)
	{
		if (value.equals("https://www.hl7.org/fhir/codesystem-concept-map-equivalence.html#concept-map-equivalence-equivalent")) {
			value = "http://hl7.org/fhir/DSTU2/valueset-concept-map-equivalence.html";
		}
		return value;
	}

	*/
	
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
