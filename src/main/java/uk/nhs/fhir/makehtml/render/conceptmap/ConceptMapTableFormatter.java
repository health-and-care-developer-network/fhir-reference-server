package uk.nhs.fhir.makehtml.render.conceptmap;

import java.util.List;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import uk.nhs.fhir.data.FhirURLConstants;
import uk.nhs.fhir.data.conceptmap.FhirConceptMapElement;
import uk.nhs.fhir.data.conceptmap.FhirConceptMapElementTarget;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.wrap.WrappedConceptMap;
import uk.nhs.fhir.makehtml.FhirFileRegistry;
import uk.nhs.fhir.makehtml.html.jdom2.Elements;
import uk.nhs.fhir.makehtml.html.panel.FhirPanel;
import uk.nhs.fhir.makehtml.html.style.FhirCSS;
import uk.nhs.fhir.makehtml.html.table.Table;
import uk.nhs.fhir.makehtml.html.table.TableFormatter;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.util.FhirVersion;

public class ConceptMapTableFormatter extends TableFormatter<WrappedConceptMap> {

	public ConceptMapTableFormatter(WrappedConceptMap conceptMap, FhirFileRegistry otherResources) {
		super(conceptMap, otherResources);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		HTMLDocSection section = new HTMLDocSection();
		
		section.addBodyElement(getElementMapsDataTable());
		section.addStyles(getStyles());
		section.addStyles(FhirPanel.getStyles());
		section.addStyles(Table.getStyles());
		
		return section;
	}

	public Element getElementMapsDataTable() {

		Element colgroup = Elements.newElement("colgroup");
		int columns = 4;
		Preconditions.checkState(100 % columns == 0, "Table column count divides 100% evenly");

    	List<Element> tableContent = Lists.newArrayList(colgroup);
        Boolean first = true;

		for (FhirConceptMapElement element: wrappedResource.getElements()) {
			if (first) {
				tableContent.add(
						Elements.withChildren("tr",
								labelledValueCell("Code", BLANK, 1, true, true),
								labelledValueCell("Equivalence", BLANK, 1, true, true),
								labelledValueCell("Target Code", BLANK, 1, true, true),
								labelledValueCell("Comments", BLANK, 1, true, true)));
				first = false;
			}
			for (FhirConceptMapElementTarget target : element.getTargets()) {
				Optional<String> comments = target.getComments();
				String displayComments = (comments != null && comments.isPresent() ) ? comments.get() : BLANK;
				
				FhirVersion implicitFhirVersion = wrappedResource.getImplicitFhirVersion();
				tableContent.add(
					Elements.withChildren("tr",
						labelledValueCell(BLANK, element.getCode(), 1, true, true),
						labelledHttpCell(FhirURL.buildOrThrow(FhirURLConstants.versionBase(implicitFhirVersion) + "/valueset-concept-map-equivalence.html", implicitFhirVersion), target.getEquivalence(),  1, true, false),
						labelledValueCell(BLANK, target.getCode(), 1, true, false),
						labelledValueCell(BLANK, displayComments, 1, true, false)));
			}
		}
        Element table =
                Elements.withAttributeAndChildren("table",
                        new Attribute("class", FhirCSS.TABLE),
                        tableContent);

		FhirPanel panel = new FhirPanel(null, table);

		return panel.makePanel();
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

    private Element labelledHttpCell(FhirURL http, String value, int colspan, boolean alwaysBig, boolean alwaysBold) {
        List<Element> cellSpans = Lists.newArrayList();
        cellSpans.add(valueSpanRef(value, http, alwaysBig));
        return cell(cellSpans, colspan);
    }
	
	private Element labelSpan(String label, boolean valueIsEmpty, boolean alwaysBold) {
		String cssClass = FhirCSS.DATA_LABEL;
		if (valueIsEmpty && !alwaysBold) {
			cssClass += " " + FhirCSS.DATA_LABEL_EMPTY;
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

    private Element valueSpanRef(String value, FhirURL http, boolean alwaysLargeText) {

        boolean largeText = alwaysLargeText || value.length() < 20;
        String fhirMetadataClass = FhirCSS.DATA_VALUE;
        if (!largeText) {
        	fhirMetadataClass += " " + FhirCSS.DATA_VALUE_SMALLTEXT;
        }

        return Elements.withAttributeAndChild("span",
            new Attribute("class", fhirMetadataClass),
            Elements.withAttributesAndText("a",
                Lists.newArrayList(
                    new Attribute("class", FhirCSS.LINK),
                    new Attribute("href", http.toLinkString())),
                value));
    }
}
