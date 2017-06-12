package uk.nhs.fhir.makehtml.render.conceptmap;

import java.util.List;
import java.util.Optional;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import ca.uhn.fhir.model.dstu2.resource.ConceptMap;
import uk.nhs.fhir.makehtml.html.FhirCSS;
import uk.nhs.fhir.makehtml.html.FhirPanel;
import uk.nhs.fhir.makehtml.html.jdom2.Elements;
import uk.nhs.fhir.makehtml.html.style.CSSRule;
import uk.nhs.fhir.makehtml.html.style.CSSStyleBlock;

public class ConceptMapTableFormatter {

	private static final String BLANK = "";

	private final ConceptMap source;

	public ConceptMapTableFormatter(ConceptMap source){
		this.source = source;
	}

	public Element getElementMapsDataTable() {

		Element colgroup = Elements.newElement("colgroup");
		int columns = 4;
		Preconditions.checkState(100 % columns == 0, "Table column count divides 100% evenly");

    	List<Element> tableContent = Lists.newArrayList(colgroup);
        Boolean first = true;

		for (ConceptMap.Element element: source.getElement()) {
			if (first) {
				tableContent.add(
						Elements.withChildren("tr",
								labelledValueCell("Code", BLANK, 1, true, true),
								labelledValueCell("Equivalence", BLANK, 1, true, true),
								labelledValueCell("Target Code", BLANK, 1, true, true),
								labelledValueCell("Comments", BLANK, 1, true, true)));
				first = false;
			}
			for (ConceptMap.ElementTarget target : element.getTarget()) {
				Optional<String> comments = Optional.ofNullable(target.getComments());
				String displayComments = (comments != null && comments.isPresent() ) ? comments.get() : BLANK;
				tableContent.add(
						Elements.withChildren("tr",
								labelledValueCell(BLANK, element.getCode(), 1, true, true),
								// STU3 url labelledHttpCell("https://www.hl7.org/fhir/codesystem-concept-map-equivalence.html#concept-map-equivalence-equivalent", target.getEquivalence(),  1, true, false),
								labelledHttpCell("http://hl7.org/fhir/DSTU2/valueset-concept-map-equivalence.html", target.getEquivalence(),  1, true, false),
								labelledValueCell(BLANK, target.getCode(), 1, true),
								labelledValueCell(BLANK, displayComments, 1, true)));
			}
		}
        Element table =
                Elements.withAttributeAndChildren("table",
                        new Attribute("class", FhirCSS.TABLE),
                        tableContent);

		String panelTitle = "Elements: " ;

		FhirPanel panel = new FhirPanel(null, table);

		return panel.makePanel();
	}


    private Element labelledValueCell(String label, String value, int colspan, boolean alwaysBig)
    {
        return labelledValueCell(label, value, colspan, alwaysBig, false);
    }

	private Element labelledValueCell(String label, String value, int colspan, boolean alwaysBig, boolean alwaysBold) {
		Preconditions.checkNotNull(value, "value data");
		
		List<Element> cellSpans = Lists.newArrayList();
		if (label.length() > 0) {
			cellSpans.add(labelSpan(label, value.isEmpty(), alwaysBold));
		}
		if (value.length() > 0) {
			cellSpans.add(valueSpan(value, alwaysBig));
		}
		
		return cell(cellSpans, colspan);
	}

    private Element labelledHttpCell(String http, String value, int colspan, boolean alwaysBig, boolean alwaysBold) {
        List<Element> cellSpans = Lists.newArrayList();
        cellSpans.add(valueSpanRef(value, http, alwaysBig));
        return cell(cellSpans, colspan);
    }
	
	private Element cell(List<? extends Content> content, int colspan) {
		return Elements.withAttributesAndChildren("td", 
			Lists.newArrayList(
				new Attribute("class", FhirCSS.METADATA_CELL),
				new Attribute("colspan", Integer.toString(colspan))),
			content);
	}
	
	private Element labelSpan(String label, boolean valueIsEmpty, boolean alwaysBold) {
		String cssClass = FhirCSS.METADATA_LABEL;
		if (valueIsEmpty && !alwaysBold) {
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

    private Element valueSpanRef(String value, String http, boolean alwaysLargeText) {

        boolean largeText = alwaysLargeText || value.length() < 20;
        String fhirMetadataClass = FhirCSS.METADATA_VALUE;
        if (!largeText) {
        	fhirMetadataClass += " " + FhirCSS.METADATA_VALUE_SMALLTEXT;
        }


        return Elements.withAttributeAndChild("span",
                    new Attribute("class", fhirMetadataClass),
                    Elements.withAttributesAndText("a",
                            Lists.newArrayList(
                                    new Attribute("class", FhirCSS.LINK),
                                    new Attribute("href", http)),
                            value));


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
