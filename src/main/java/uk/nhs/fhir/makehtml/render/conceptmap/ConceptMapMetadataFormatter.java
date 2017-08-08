package uk.nhs.fhir.makehtml.render.conceptmap;

import java.util.List;
import java.util.Optional;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.data.FhirURL;
import uk.nhs.fhir.makehtml.data.wrap.WrappedConceptMap;
import uk.nhs.fhir.makehtml.html.FhirCSS;
import uk.nhs.fhir.makehtml.html.FhirPanel;
import uk.nhs.fhir.makehtml.html.jdom2.Elements;
import uk.nhs.fhir.makehtml.html.style.CSSRule;
import uk.nhs.fhir.makehtml.html.style.CSSStyleBlock;

public class ConceptMapMetadataFormatter {

	private final WrappedConceptMap source;

	public ConceptMapMetadataFormatter(WrappedConceptMap conceptMap){
		this.source = conceptMap;
	}
	
	public Element getMetadataTable() {
		
		// These are all required and so should always be present
		Optional<String>  name = source.getName();

		String status = source.getStatus().toString();

		Optional<String> version = source.getVersion();

		String sourceDesc = source.getSource();
		String targetDesc = source.getTarget();
		
		String gridName = name.get();
		if (version.isPresent()) {
			gridName += " (v" + version.get() + ")";
		}


		Element colgroup = Elements.newElement("colgroup");

		List<Element> tableContent = Lists.newArrayList(colgroup);

		tableContent.add(
			Elements.withChildren("tr",
				labelledValueCell("Name", gridName, 4, true)));
		tableContent.add(
			Elements.withChildren("tr",
					labelledValueCell("Status", status, 4)));

		tableContent.add(
				Elements.withChildren("tr",
						labelledValueCell("Source", sourceDesc, 4, true)
				));
		tableContent.add(
				Elements.withChildren("tr",
						labelledValueCell("Target", targetDesc, 4, true)
						));



		Element table = 
			Elements.withAttributeAndChildren("table",
				new Attribute("class", FhirCSS.TABLE),
				tableContent);
		
		String panelTitleName =  name.get();
		String panelTitle = "ConceptMap: " + panelTitleName;
		
		FhirPanel panel = new FhirPanel(panelTitle, table);
		
		return panel.makePanel();
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
				new Attribute("class", FhirCSS.METADATA_CELL),
				new Attribute("colspan", Integer.toString(colspan))),
			content);
	}
	
	private Element labelSpan(String label, boolean valueIsEmpty) {
		String cssClass = FhirCSS.METADATA_LABEL;
		if (valueIsEmpty) {
			cssClass += " " + FhirCSS.METADATA_LABEL_EMPTY;
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
		String fhirMetadataClass = FhirCSS.METADATA_VALUE;
		if (!largeText) fhirMetadataClass += " " + FhirCSS.METADATA_VALUE_SMALLTEXT;
		
		if (url) {
			return Elements.withAttributeAndChild("span",
				new Attribute("class", fhirMetadataClass),
				Elements.withAttributesAndText("a",
					Lists.newArrayList(
						new Attribute("class", FhirCSS.LINK),
						new Attribute("href", FhirURL.buildOrThrow(value, source.getImplicitFhirVersion()).toLinkString())),
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
				Lists.newArrayList("." + FhirCSS.METADATA_CELL),
				Lists.newArrayList(
					new CSSRule("border", "1px solid #f0f0f0"))));
		styles.add(
				new CSSStyleBlock(
					Lists.newArrayList("." + FhirCSS.METADATA_LABEL, "." + FhirCSS.TELECOM_NAME),
					Lists.newArrayList(
						new CSSRule("color", "#808080"),
						new CSSRule("font-weight", "bold"),
						new CSSRule("font-size", "13"))));
		styles.add(
				new CSSStyleBlock(
					Lists.newArrayList("." + FhirCSS.METADATA_LABEL_EMPTY),
					Lists.newArrayList(
						new CSSRule("color", "#D0D0D0"),
						new CSSRule("font-weight", "normal"))));
		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList("." + FhirCSS.METADATA_VALUE, "." + FhirCSS.TELECOM_VALUE),
				Lists.newArrayList(
					new CSSRule("color", "#000000"),
					new CSSRule("font-size", "13"))));
		styles.add(
				new CSSStyleBlock(
					Lists.newArrayList("." + FhirCSS.METADATA_VALUE_SMALLTEXT),
					Lists.newArrayList(
						new CSSRule("font-size", "10"))));
		styles.add(
				new CSSStyleBlock(
					Lists.newArrayList("." + FhirCSS.METADATA_BLOCK_TITLE),
					Lists.newArrayList(
							new CSSRule("color", "#808080"),
							new CSSRule("font-weight", "bold"),
							new CSSRule("text-decoration", "underline"),
							new CSSRule("font-size", "13"))));
		
		return styles;
	}
}
