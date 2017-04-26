package uk.nhs.fhir.makehtml.html;

import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.model.primitive.UriDt;
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

public class ValueSetMetadataFormatter {

	private static final String BLANK = "";

	private final ValueSet source;

	public ValueSetMetadataFormatter(ValueSet source){
		this.source = source;
	}
	
	public Element getMetadataTable() {
		
		// These are all required and so should always be present

		Optional<String>  url = Optional.ofNullable(source.getUrl());
		Optional<String>  name = Optional.ofNullable(source.getName());

		String status = source.getStatus().toString();

		Optional<String> OID= Optional.empty();
		Optional<String> reference = Optional.empty();

		for (ExtensionDt extension : source.getUndeclaredExtensions())
		{
			if (extension.getUrl().contains("http://hl7.org/fhir/StructureDefinition/valueset-sourceReference")) {
				UriDt uri = (UriDt) extension.getValue();
				reference = Optional.ofNullable(uri.getValueAsString());
			}
			if (extension.getUrl().contains("http://hl7.org/fhir/StructureDefinition/valueset-oid")) {
				UriDt uri = (UriDt) extension.getValue();
				OID = Optional.ofNullable(uri.getValueAsString());
			}

		}

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
				labelledValueCell("Name", gridName, 2, true),
				labelledValueCell("URL", url.get(), 2, true)));
		tableContent.add(
			Elements.withChildren("tr",
					labelledValueCell("Status", status, 1),
					labelledValueCell("Version", version, 1),
					labelledValueCell("Last updated", displayDate, 1),

						labelledValueCell("Experimental", displayExperimental, 1)
						));

		tableContent.add(
				Elements.withChildren("tr",
						labelledValueCell("Description", description.get(), 4, true)
				));
		tableContent.add(
				Elements.withChildren("tr",
						labelledValueCell("Publisher", publisher, 4)
						));

		tableContent.add(
				Elements.withChildren("tr",
						labelledValueCell("Copyright", copyright, 4)
				));
		if (reference.isPresent()) {
			tableContent.add(
					Elements.withChildren("tr",
							labelledValueCell("Reference", reference.get(), 4, true)
					));
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
				new Attribute("class", "fhir-table"),
				tableContent);
		
		String panelTitleName =  name.get();
		String panelTitle = "ValueSet: " + panelTitleName;
		
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
