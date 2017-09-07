package uk.nhs.fhir.makehtml.render.valueset;

import javax.xml.parsers.ParserConfigurationException;

import uk.nhs.fhir.data.wrap.WrappedValueSet;
import uk.nhs.fhir.makehtml.FhirFileRegistry;
import uk.nhs.fhir.makehtml.html.table.TableFormatter;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;

public class ValueSetTableFormatter extends TableFormatter<WrappedValueSet> {
	
	public ValueSetTableFormatter(WrappedValueSet wrappedResource, FhirFileRegistry otherResources) {
		super(wrappedResource, otherResources);
	}

	//private static final String BLANK = "";

    //private WrappedConceptMap conceptMap = null;
	
	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		
		boolean filterPresent = wrappedResource.hasComposeIncludeFilter();
		if (filterPresent) {
			return new FilteredValueSetTableFormatter(wrappedResource, otherResources).makeSectionHTML();
		} else {
			return new ConceptsValueSetTableFormatter(wrappedResource, otherResources).makeSectionHTML();
		}
		
		/*HTMLDocSection section = new HTMLDocSection();
		
		Element metadataPanel = getConceptDataTable();
		section.addBodyElement(metadataPanel);
		
		Table.getStyles().forEach(section::addStyle);
		FhirPanel.getStyles().forEach(section::addStyle);
		LinkCell.getStyles().forEach(section::addStyle);
		ValueWithInfoCell.getStyles().forEach(section::addStyle);
		TableFormatter.getStyles().forEach(section::addStyle);
		
		return section;*/
	}
	
	/*private Element getConceptDataTable() {
		
		
		FhirValueSetCompose compose = wrappedResource.getCompose();

        Boolean first = true;
        Optional<FhirCodeSystemConcepts> codeSystem = wrappedResource.getCodeSystem();
        if (codeSystem.isPresent()) {
			String system = codeSystem.get().getSystem();
	        
	        for (FhirCodeSystemConcept concept: codeSystem.get().getConcepts()) {
	        	String description = concept.getDescription().orElse(BLANK);
	            String definition = concept.getDefinition().orElse(BLANK);
	            
	            if (first) {
	                tableContent.add(codeHeader(true));
	                tableContent.add(codeSystem(system, true, false, "Inline code system"));
	            }
	            
	            tableContent.add(
	                        codeContent(concept.getCode(), description, definition, getConceptMapping(concept.getCode())));
	            first = false;
	        }
        }


		for (String uri :compose.getImportUris()) {
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
                            labelledValueCell(BLANK, uri, 1, true),
                            labelledValueCell(BLANK, "", 1, true, true, false),
                            labelledValueCell(BLANK, "", 1, true, true, false)));
        }*/
        /*

        Include from an External CodeSystem

         */
		/*for (FhirValueSetComposeInclude include: compose.getIncludes()) {


            Boolean filterFirst = true;
            for (FhirValueSetComposeIncludeFilter filter: include.getFilters()) {
                // Display System first. Filter or Included must follow
                if (filterFirst && include.getSystem() != null) {
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
			for (FhirCodeSystemConcept concept : include.getConcepts()) {

				boolean hasSystem = (include.getSystem() != null);
				
                if (first 
                  && hasSystem) {
                    tableContent.add(codeHeader(true));
                    first = false;
                }
                
                if (composeFirst 
                  && hasSystem) {
                    tableContent.add(codeSystem(include.getSystem(), false, true,"External Code System"));
                    composeFirst = false;
                }

                String description = concept.getDescription().orElse(BLANK);
                
                // Add the code details
                tableContent.add(codeContent(concept.getCode(), description, BLANK, getConceptMapping(concept.getCode())));

            }

		}*/
		/*

        Exclude from External CodeSystem

         */
        /*for (FhirValueSetComposeInclude exclude: compose.getExcludes()) {


            if (exclude.getSystem() != null) {
                Optional<String> version = exclude.getVersion();
                String displayVersion = (version != null && version.isPresent() ) ? version.get() : BLANK;
                tableContent.add(
                        Elements.withChildren("tr",
                                labelledValueCell("System", exclude.getSystem(), 2, true),
                                labelledValueCell("Version", displayVersion, 2, true)));
            }

            for (FhirCodeSystemConcept concept : exclude.getConcepts()) {
                String description = concept.getDescription().orElse(BLANK);

                if (first) {
                    tableContent.add(codeHeader(true));
                }
                codeContent(concept.getCode(), BLANK, description, getConceptMapping(concept.getCode()));

                first  = false;
            }

        }


		Element table =
				Elements.withAttributeAndChildren("table",
						new Attribute("class", FhirCSS.TABLE),
						tableContent);


		String panelTitle = null;

		FhirPanel panel = new FhirPanel(panelTitle, table);

		return panel.makePanel();
	}

	private String getConceptMapping(String code) {
        String mapping = BLANK;
        if (conceptMap != null) {
            for (FhirConceptMapElement mapElement : conceptMap.getElements()) {
                if (code.equals(mapElement.getCode()) 
                  && mapElement.getTargets().size() > 0) {
                    mapping = "~" + mapElement.getTargets().get(0).getCode();
                }
            }
        }
        return mapping;
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
	
	private Element valueSpan(String value, boolean alwaysLargeText, boolean reference , boolean internal, String hint) {
		boolean url = (value.startsWith("http://") || value.startsWith("https://"))
		  && new UrlValidator().testSingleUrl(value);
		boolean largeText = alwaysLargeText || value.length() < 20;
		String fhirMetadataClass = FhirCSS.DATA_VALUE;
		if (!largeText) fhirMetadataClass += " " + FhirCSS.DATA_VALUE_SMALLTEXT;
		
		if (url) {
		    if (reference) {
                return Elements.withAttributeAndChild("span",
                    new Attribute("class", fhirMetadataClass),
                    Elements.withAttributesAndChildren("a",
                        Lists.newArrayList(
                            new Attribute("class", FhirCSS.LINK),
                            new Attribute("href", FhirURL.buildOrThrow(ValuesetLinkFix.fixLink(value, getResourceVersion()), getResourceVersion()).toLinkString()),
                            new Attribute("title", hint)),
                        Lists.newArrayList(
                            new Text(value),
                            Elements.withAttributes("img",
                                Lists.newArrayList(
                                    new Attribute("src", FhirIcon.REFERENCE.getUrl()),
                                    new Attribute("class", FhirCSS.TREE_RESOURCE_ICON)))))); //value +
            } else if (internal) {
                return Elements.withAttributeAndChildren("span",
                    new Attribute("class", fhirMetadataClass),
                    Lists.newArrayList(
                        Elements.withAttributesAndText("a",
                            Lists.newArrayList(
                                new Attribute("class", FhirCSS.LINK),
                                new Attribute("href", FhirURL.buildOrThrow(ValuesetLinkFix.fixLink(value, getResourceVersion()), getResourceVersion()).toLinkString()),
                                new Attribute("title", hint)),
                            value)
                //        ,new Text(" (internal)") // Removed internal, using icon for external instead
                    ));
            } else {
                return Elements.withAttributeAndChild("span",
                    new Attribute("class", fhirMetadataClass),
                    Elements.withAttributesAndText("a",
                        Lists.newArrayList(
                            new Attribute("class", FhirCSS.LINK),
                            new Attribute("href", FullFhirURL.buildOrThrow(ValuesetLinkFix.fixLink(value, getResourceVersion()), getResourceVersion()).toLinkString()),
                            new Attribute("title", hint)),
                        value));
            }
			
		} else {
			return Elements.withAttributeAndText("span",
                new Attribute("class", fhirMetadataClass),
                value);
		}
	}*/
}
