package uk.nhs.fhir.render.format.conceptmap;

import java.util.List;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import uk.nhs.fhir.data.conceptmap.FhirConceptMapElement;
import uk.nhs.fhir.data.conceptmap.FhirConceptMapElementTarget;
import uk.nhs.fhir.data.conceptmap.FhirConceptMapGroup;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.wrap.WrappedConceptMap;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.TableFormatter;
import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.style.FhirCSS;
import uk.nhs.fhir.render.html.table.Table;
import uk.nhs.fhir.util.FhirURLConstants;
import uk.nhs.fhir.util.FhirVersion;

public class ConceptMapTableFormatter extends TableFormatter<WrappedConceptMap> {

	public ConceptMapTableFormatter(WrappedConceptMap conceptMap) {
		super(conceptMap);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		HTMLDocSection section = new HTMLDocSection();

		for (FhirConceptMapGroup group : wrappedResource.getMappingGroups()) {
			section.addBodyElement(getMappingGroupPanel(group));
		}
		section.addStyles(getStyles());
		section.addStyles(FhirPanel.getStyles());
		section.addStyles(Table.getStyles());
		
		return section;
	}

	public Element getMappingGroupPanel(FhirConceptMapGroup group) {

		Element colgroup = Elements.newElement("colgroup");
		int columns = 4;
		Preconditions.checkState(100 % columns == 0, "Table column count divides 100% evenly");

    	List<Element> tableContent = Lists.newArrayList(colgroup);
        Boolean first = true;
        for (FhirConceptMapElement element : group.getMappings()) {
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
				
				FhirVersion implicitFhirVersion = getResourceVersion();
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

        String labelClass = FhirCSS.INFO_PLAIN;
        String codeSystemIdClass = FhirCSS.DATA_LABEL;
        
        Element panelContents =
        	Elements.withChildren("div",
        		Elements.withChildren("div",
        			Elements.withAttributeAndText("span", new Attribute("class", labelClass), "From code system: "),
        			Elements.withAttributeAndText("span", new Attribute("class", codeSystemIdClass), group.getFromCodeSystem())),
        		Elements.withChildren("div",
        			Elements.withAttributeAndText("span", new Attribute("class", labelClass), "To code system: "),
        			Elements.withAttributeAndText("span", new Attribute("class", codeSystemIdClass), group.getToCodeSystem())
        			),
    			Elements.newElement("br"),
        		table);

		return new FhirPanel("Mappings Group", panelContents).makePanel();
	}

	private Element labelledValueCell(String label, String value, int colspan, boolean alwaysBig, boolean alwaysBold) {
		String nonNullValue = Preconditions.checkNotNull(value, "value data");
		boolean hasValue = !nonNullValue.isEmpty();
		
		List<Element> cellSpans = Lists.newArrayList();
		
		if (label.length() > 0) {
			cellSpans.add(labelSpan(label, hasValue, alwaysBold));
		}
		
		if (hasValue) {
			cellSpans.add(valueSpan(nonNullValue, alwaysBig));
		}
		
		return cell(cellSpans, colspan);
	}

    private Element labelledHttpCell(FhirURL http, String value, int colspan, boolean alwaysBig, boolean alwaysBold) {
        List<Element> cellSpans = Lists.newArrayList();
        cellSpans.add(valueSpanRef(value, http, alwaysBig));
        return cell(cellSpans, colspan);
    }
	
	private Element labelSpan(String label, boolean hasValue, boolean alwaysBold) {
		String cssClass = FhirCSS.DATA_LABEL;
		if (!hasValue && !alwaysBold) {
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
