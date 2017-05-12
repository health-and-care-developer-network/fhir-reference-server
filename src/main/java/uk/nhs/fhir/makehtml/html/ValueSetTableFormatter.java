package uk.nhs.fhir.makehtml.html;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.ConceptMap;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.model.primitive.UriDt;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Text;
import uk.nhs.fhir.makehtml.CSSStyleBlock;
import uk.nhs.fhir.makehtml.HTMLDocSection;
import uk.nhs.fhir.makehtml.data.FhirIcon;
import uk.nhs.fhir.util.Elements;

import java.util.List;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

public class ValueSetTableFormatter extends MetadataTableFormatter {

	private static final String BLANK = "";

    private ConceptMap conceptMap = null;
	
	@Override
	public HTMLDocSection makeSectionHTML(IBaseResource source) throws ParserConfigurationException {
		ValueSet valueSet = (ValueSet)source;
		HTMLDocSection section = new HTMLDocSection();
		
		Element metadataPanel = getConceptDataTable(valueSet);
		section.addBodyElement(metadataPanel);
		
		return section;
	}

	public Element getConceptDataTable(ValueSet source) {


		int columns = 4;
        Boolean filterPresent = false;
        conceptMap = getConceptMap(source);

        if (conceptMap != null) {
            columns = 5;
        }
        for (ValueSet.ComposeInclude include: source.getCompose().getInclude()) {
            if (include.getFilter().size() > 0) filterPresent = true;
        }

        Element colgroup = Elements.newElement("colgroup");

        Preconditions.checkState(100 % columns == 0, "Table column count divides 100% evenly");

        int percentPerColumn = 100/columns;

        // Display when ConceptMap present
        if (columns == 5) {
            percentPerColumn = 10;
            colgroup.addContent(
                    Elements.withAttributes("col",
                            Lists.newArrayList(
                                    new Attribute("width", Integer.toString(3 * percentPerColumn) + "%"))));
            colgroup.addContent(
                    Elements.withAttributes("col",
                            Lists.newArrayList(
                                    new Attribute("width", Integer.toString(1 * percentPerColumn) + "%"))));
            colgroup.addContent(
                    Elements.withAttributes("col",
                            Lists.newArrayList(
                                    new Attribute("width", Integer.toString(2 * percentPerColumn) + "%"))));
            colgroup.addContent(
                    Elements.withAttributes("col",
                            Lists.newArrayList(
                                    new Attribute("width", Integer.toString(3 * percentPerColumn) + "%"))));
            colgroup.addContent(
                    Elements.withAttributes("col",
                            Lists.newArrayList(
                                    new Attribute("width", Integer.toString(1 * percentPerColumn) + "%"))));

        }
        else if (!filterPresent && columns == 4)
        {
            // Basic list of codes

            percentPerColumn = 10;
            colgroup.addContent(
                    Elements.withAttributes("col",
                            Lists.newArrayList(
                                    new Attribute("width", Integer.toString(3 * percentPerColumn) + "%"))));
            colgroup.addContent(
                    Elements.withAttributes("col",
                            Lists.newArrayList(
                                    new Attribute("width", Integer.toString(1 * percentPerColumn) + "%"))));
            colgroup.addContent(
                    Elements.withAttributes("col",
                            Lists.newArrayList(
                                    new Attribute("width", Integer.toString(2 * percentPerColumn) + "%"))));
            colgroup.addContent(
                    Elements.withAttributes("col",
                            Lists.newArrayList(
                                    new Attribute("width", Integer.toString(4 * percentPerColumn) + "%"))));
        } else {
            for (int i = 0; i < columns; i++) {
                colgroup.addContent(
                        Elements.withAttributes("col",
                                Lists.newArrayList(
                                        new Attribute("width", Integer.toString(percentPerColumn) + "%"))));
            }
        }




        List<Element> tableContent = Lists.newArrayList(colgroup);
         /*

        Include from an Internal CodeSystem

         */
        Boolean first = true;
        for (ValueSet.CodeSystemConcept concept: source.getCodeSystem().getConcept()) {
            Optional<String> display = Optional.ofNullable(concept.getDisplay());
            String displayDisplay = (display!=null && display.isPresent()) ? display.get() : BLANK;

            Optional<String> system = Optional.ofNullable(source.getCodeSystem().getSystem());
            String displaySystem = (system!=null && system.isPresent()) ? system.get() : BLANK;

            Optional<String> definition = Optional.ofNullable(concept.getDefinition());
            String displayDefinition = (definition!=null && definition.isPresent()) ? definition.get() : BLANK;
            if (first) {
                tableContent.add(codeHeader(true));
                tableContent.add(codeSystem(displaySystem,true, false, "Inline code system"));
            }
            tableContent.add(
                        codeContent(concept.getCode(), displayDisplay, displayDefinition, getConceptMapping(concept.getCode())));
            first = false;
        }

		for (UriDt uri :source.getCompose().getImport()) {
            if (first)
            {
                tableContent.add(
                        Elements.withChildren("tr",
                                labelledValueCell(BLANK, BLANK, 1, true, true,false),
                                labelledValueCell("URI", BLANK, 1, true, true,false),

                                labelledValueCell(BLANK, BLANK, 1, true, true,false),
                                labelledValueCell(BLANK, BLANK, 1, true, true,false)));
            }
            tableContent.add(
                    Elements.withChildren("tr",
                            labelledValueCell("Import",BLANK , 1, true, true, false,false,"Import the contents of another ValueSet"),
                            labelledValueCell(BLANK, uri.getValue(), 1, true),
                            labelledValueCell(BLANK, "", 1, true, true, false),
                            labelledValueCell(BLANK, "", 1, true, true, false)));
        }
        /*

        Include from an External CodeSystem

         */
		for (ValueSet.ComposeInclude include: source.getCompose().getInclude()) {


            Boolean filterFirst = true;
            for (ValueSet.ComposeIncludeFilter filter: include.getFilter()) {
                // Display System first. Filter or Included must follow
                if (filterFirst && include.getSystem() != null) {
                    // May not have an included CodeSystem
                    Optional<String> version = Optional.ofNullable(include.getVersion());
                    String displayVersion = (version != null && version.isPresent() ) ? version.get() : BLANK;
                    tableContent.add(codeHeader(false));
                    tableContent.add(
                            codeSystem( include.getSystem() ,false, true, "External Code System"));
                    first = false;
                }


                tableContent.add(
                        Elements.withChildren("tr",
                                labelledValueCell("Filter", "", 1, true, true, false),
                                labelledValueCell("Property", filter.getProperty(), 1, true),
                                labelledValueCell("Operation", filter.getOp(), 1, true),
                                labelledValueCell("Value", filter.getValue() , 1, true)));
                // Added a filter so force column header

            }
            Boolean composeFirst = true;
			for (ValueSet.ComposeIncludeConcept concept : include.getConcept()) {

                if (first && include.getSystem() != null) {
                    // May not have an included CodeSystem
                    Optional<String> version = Optional.ofNullable(include.getVersion());
                    String displayVersion = (version != null && version.isPresent()) ? version.get() : BLANK;
                    tableContent.add(codeHeader(true));
                    first = false;
                }
                if (composeFirst && include.getSystem() != null) {
                    tableContent.add(codeSystem( include.getSystem(), false, true,"External Code System"));
                    composeFirst = false;
                }

                Optional<String> display = Optional.ofNullable(concept.getDisplay());
                String displayDisplay = (display != null && display.isPresent()) ? display.get() : BLANK;
                // Add the code details
                tableContent.add(codeContent(concept.getCode(), displayDisplay, BLANK, getConceptMapping(concept.getCode())));

            }

		}
		/*

        Exclude from External CodeSystem

         */
        for (ValueSet.ComposeInclude exclude: source.getCompose().getExclude()) {


            if (exclude.getSystem() != null) {
                Optional<String> version = Optional.ofNullable(exclude.getVersion());
                String displayVersion = (version != null && version.isPresent() ) ? version.get() : BLANK;
                tableContent.add(
                        Elements.withChildren("tr",
                                labelledValueCell("System", exclude.getSystem(), 2, true),
                                labelledValueCell("Version", displayVersion, 2, true)));
            }


            for (ValueSet.ComposeIncludeConcept concept : exclude.getConcept()) {
                Optional<String> display = Optional.ofNullable(concept.getDisplay());
                String displayDisplay = (display != null && display.isPresent()) ? display.get() : BLANK;

                if (first) {
                    tableContent.add(codeHeader(true));
                }
                codeContent(concept.getCode(), BLANK, displayDisplay, getConceptMapping(concept.getCode()));

                first  = false;
            }

        }


		Element table =
				Elements.withAttributeAndChildren("table",
						new Attribute("class", "fhir-table"),
						tableContent);


		String panelTitle = null;

		FhirPanel panel = new FhirPanel(panelTitle, table);

		return panel.makePanel();
	}
    private String getConceptMapping(String code)
    {
        String mapping = BLANK;
        if (conceptMap != null) {
            for (ConceptMap.Element mapElement : conceptMap.getElement()) {
                if (code.equals(mapElement.getCode()) && mapElement.getTarget().size() > 0) {
                    mapping = "~" + mapElement.getTarget().get(0).getCode();
                }
            }
        }
        return mapping;
    }
    
	private ConceptMap getConceptMap(ValueSet source)
    {
        // Included ConceptMaps - this is coded so ConceptMap can be a separate resource
        ConceptMap conceptMap = null;

        for (IResource resource : source.getContained().getContainedResources())
        {
            if (resource instanceof ConceptMap)
            {
                conceptMap = (ConceptMap) resource;
            }
        }
            
        return conceptMap;
    }
	
    private Element codeSystem(String displaySystem, Boolean internal, Boolean reference, String hint)
    {
        if (conceptMap == null) {
            return Elements.withChildren("tr",
                    labelledValueCell(BLANK, displaySystem, 1, true, false, reference, internal, hint),
                    labelledValueCell(BLANK, BLANK, 1, true),
                    labelledValueCell(BLANK, BLANK, 1, true),
                    labelledValueCell(BLANK, BLANK, 1, true));
        }
        else
        {
            return Elements.withChildren("tr",
                    labelledValueCell(BLANK, displaySystem, 1, true, false, reference, internal, hint),
                    labelledValueCell(BLANK, BLANK, 1, true),
                    labelledValueCell(BLANK, BLANK, 1, true),
                    labelledValueCell(BLANK, BLANK, 1, true),
                    labelledValueCell(BLANK, BLANK, 1, true));
        }
    }
    private Element codeContent(String code, String display, String definition, String mapping)
    {
        if (conceptMap == null) {

            return Elements.withChildren("tr",
                    labelledValueCell(BLANK, BLANK, 1, true),
                    labelledValueCell(BLANK, code, 1, true),
                    labelledValueCell(BLANK, display, 1, true),
                    labelledValueCell(BLANK, definition, 1, true));
        } else {
            return Elements.withChildren("tr",
                    labelledValueCell(BLANK, BLANK, 1, true),
                    labelledValueCell(BLANK, code, 1, true),
                    labelledValueCell(BLANK, display, 1, true),
                    labelledValueCell(BLANK, definition, 1, true),
                    labelledValueCell(BLANK, mapping, 1, true));
        }
    }
    private Element codeHeader(Boolean full)
    {
	    if (full) {
            if (conceptMap == null) {
                return Elements.withChildren("tr",
                        labelledValueCell("CodeSystem", BLANK, 1, false, true, true),
                        labelledValueCell("Code", BLANK, 1, true, true, false),
                        labelledValueCell("Display", BLANK, 1, true, true, false),
                        labelledValueCell("Definition", BLANK, 1, false, true, false));
            } else {
                return Elements.withChildren("tr",
                        labelledValueCell("CodeSystem", BLANK, 1, false, true, true),
                        labelledValueCell("Code", BLANK, 1, true, true, false),
                        labelledValueCell("Display", BLANK, 1, true, true, false),
                        labelledValueCell("Definition", BLANK, 1, false, true, false),
                        labelledValueCell("Mapping", BLANK, 1, false, true, false));
            }
	    } else {
            if (conceptMap == null) {
                return Elements.withChildren("tr",
                        labelledValueCell("CodeSystem", BLANK, 1, false, true, true),
                        labelledValueCell(BLANK, BLANK, 1, true, true, false),
                        labelledValueCell(BLANK, BLANK, 1, true, true, false),
                        labelledValueCell(BLANK, BLANK, 1, false, true, false));
            } else {
                return Elements.withChildren("tr",
                        labelledValueCell("CodeSystem", BLANK, 1, false, true, true),
                        labelledValueCell(BLANK, BLANK, 1, true, true, false),
                        labelledValueCell(BLANK, BLANK, 1, true, true, false),
                        labelledValueCell(BLANK, BLANK, 1, false, true, false),
                        labelledValueCell(BLANK, BLANK, 1, false, true, false));
            }
        }
    }

    private Element labelledValueCell(String label, String value, int colspan, boolean alwaysBig, boolean alwaysBold, boolean reference)
    {
        return labelledValueCell(label, value, colspan, alwaysBig, alwaysBold, reference,false,"");
    }

	private Element labelledValueCell(String label, String value, int colspan, boolean alwaysBig, boolean alwaysBold, boolean reference, boolean internal, String hint) {
		Preconditions.checkNotNull(value, "value data");
		
		List<Element> cellSpans = Lists.newArrayList();
		if (label.length() > 0) {
			cellSpans.add(labelSpan(label, value.isEmpty(), alwaysBold));
		}
		if (value.length() > 0) {
			cellSpans.add(valueSpan(value, alwaysBig, reference, internal, hint));
		}
		
		return cell(cellSpans, colspan);
	}
	
	private Element labelSpan(String label, boolean valueIsEmpty, boolean alwaysBold) {
		String cssClass = "fhir-metadata-label";
		if (valueIsEmpty && !alwaysBold) {
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
	
	private Element valueSpan(String value, boolean alwaysLargeText, boolean reference , boolean internal, String hint) {
		boolean url = (value.startsWith("http://") || value.startsWith("https://"));
		boolean largeText = alwaysLargeText || value.length() < 20;
		String fhirMetadataClass = "fhir-metadata-value";
		if (!largeText) fhirMetadataClass += " fhir-metadata-value-smalltext";
		
		if (url) {
		    if (reference) {
                return Elements.withAttributeAndChild("span",
                        new Attribute("class", fhirMetadataClass),
                        Elements.withAttributesAndChildren("a",
                                Lists.newArrayList(
                                        new Attribute("class", "fhir-link"),
                                        new Attribute("href", Dstu2Fix.dstu2links(value)),
                                         new Attribute("title", hint)),
                                Lists.newArrayList(
                                        new Text(value),
                                        Elements.withAttributes("img",
                                                Lists.newArrayList(
                                                        new Attribute("src", FhirIcon.REFERENCE.getUrl()),
                                                        new Attribute("class", "fhir-tree-resource-icon")))
                                ))); //value +
            } else if (internal) {
                return Elements.withAttributeAndChildren("span",
                        new Attribute("class", fhirMetadataClass),
                        Lists.newArrayList(
                            Elements.withAttributesAndText("a",

                                    Lists.newArrayList(
                                            new Attribute("class", "fhir-link"),
                                            new Attribute("href", Dstu2Fix.dstu2links(value)),
                                            new Attribute("title", hint)),
                                    value)
                        //        ,new Text(" (internal)") // Removed internal, using icon for external instead
                        ));
            } else {
                return Elements.withAttributeAndChild("span",
                        new Attribute("class", fhirMetadataClass),
                        Elements.withAttributesAndText("a",
                                Lists.newArrayList(
                                        new Attribute("class", "fhir-link"),
                                        new Attribute("href", Dstu2Fix.dstu2links(value)),
                                        new Attribute("title", hint)),
                                value));
            }
			
		} else {
			return Elements.withAttributeAndText("span",
                    new Attribute("class", fhirMetadataClass),
                    value);
		}
	}
}
