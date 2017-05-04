package uk.nhs.fhir.makehtml.old;

import static uk.nhs.fhir.makehtml.old.XMLParserUtils.getFirstNamedChildValue;

import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;

public class ValueSetHTMLMakerOLD extends HTMLMakerOLD {

	/**
     * Make the html narrative section for the ValueSet described in this XML
     *
     * @param thisDoc   The XML Document as an org.w3c.dom.Document
     *
     * @return          Valid xhtml fully describing the ValueSet
     */
	@Override
    public String makeHTML(Document thisDoc) {

        StringBuilder sb = new StringBuilder();

        sb.append("<div style='font-family: sans-serif;' xmlns='http://www.w3.org/1999/xhtml'>\n");

        // Here we need to get the name element
        
        /*
         * AH: The FHIR server shows this meta-data anyway, so don't think we need it here?
         
        sb.append("<table>");
        sb.append("<tr><td>Name:</td><td>" + getValueSetName(thisDoc) + "</td></tr>\n");
        sb.append("<tr><td>Version:</td><td>" + getValueSetVersion(thisDoc) + "</td></tr>\n");

        sb.append("<tr><td>Publisher:</td><td>" + getValueSetPublisher(thisDoc) + "</td></tr>\n");
        sb.append("<tr><td>URL:</td><td>" + getValueSetURL(thisDoc) + "</td></tr>\n");
        sb.append("<tr><td>Status:</td><td>" + getValueSetStatus(thisDoc) + "</td></tr>\n");
        sb.append("</table>");
		*/
        
        //<editor-fold defaultstate="collapsed" desc="Here we go through any compose sections where we point to other valuesets">
        List<Element> composeSet = XMLParserUtils.descendantsList(thisDoc, "compose");
        
        if(composeSet.size() > 0) {
            sb.append("<h4>Composed from</h4>");

            Element composeElement = (Element) composeSet.get(0);


            // The compose can be one or more of Import, Inlude and Exclude sections
            // Imports is dead easy...
            for (Element importRef : composeElement.getDescendants(new ElementFilter("import"))) {
                sb.append("<p><b>Import:</b> " + importRef.getAttribute("value") + "<br /></p>");
            }

            // Includes is more tricky...
            for (Element includeRef : composeElement.getDescendants(new ElementFilter("include"))) {

                sb.append("<p><table><tr><td><b>Code System:</b></td><td>" + getFirstNamedChildValue(includeRef, "system") + "</td></tr>");

                List<Element> filterList = XMLParserUtils.descendantsList(includeRef, "filter");
                if (filterList.size()>0) {
                	sb.append("<tr><td colspan='2'><b>Filters:</b></td></tr>");
                }
                for(int j = 0; j < filterList.size(); j++) {
                    Element theFilter = filterList.get(j);
                    sb.append("<tr><td>Property:</td><td>" + getFirstNamedChildValue(theFilter, "property") + "</td></tr>");
                    sb.append("<tr><td>Operation:</td><td>" + getFirstNamedChildValue(theFilter, "op") + "</td></tr>");
                    sb.append("<tr><td>Value:</td><td>" + getFirstNamedChildValue(theFilter, "value") + "</td></tr>");
                }
                
                List<Element> includeList = XMLParserUtils.descendantsList(includeRef, "concept");
                if (includeList.size()>0) {
                	sb.append("<tr><td colspan='2'><b>Includes:</b></td></tr>");
                }
                for(int j = 0; j < includeList.size(); j++) {
                    Element theInclude = includeList.get(j);
                    sb.append("<tr><td colspan='2'><li>");
                    sb.append("<b>code:</b> " + getFirstNamedChildValue(theInclude, "code"));
                    sb.append(": ");
                    sb.append("<b>display:</b> " + getFirstNamedChildValue(theInclude, "display"));
                    sb.append("</li></td></tr>");
                    
                }
                
                sb.append("</table></p>");
            }

            // Excludes is identical to Includes...
            for (Element excludeRef : composeElement.getDescendants(new ElementFilter("exclude"))) {

                sb.append("<p><table><tr><td><b>Exclude:</b></td><td>" + getFirstNamedChildValue(excludeRef, "system") + "</td></tr>");

                List<Element> filterList = XMLParserUtils.descendantsList(excludeRef, "filter");
                for(int j = 0; j < filterList.size(); j++) {
                    Element theFilter = filterList.get(j);

                    sb.append("<tr><td>Property:</td><td>" + getFirstNamedChildValue(theFilter, "property") + "</td></tr>");
                    sb.append("<tr><td>Operation:</td><td>" + getFirstNamedChildValue(theFilter, "op") + "</td></tr>");
                    sb.append("<tr><td>Value:</td><td>" + getFirstNamedChildValue(theFilter, "value") + "</td></tr>");
                }
                sb.append("</table></p>");
            }

        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Here we go through any codeSystem items">
        List<Element> codeSystemSet = XMLParserUtils.descendantsList(thisDoc, "codeSystem");

        List<Element> conceptMaps = XMLParserUtils.descendantsList(thisDoc, "ConceptMap");

        if(codeSystemSet.size() > 0) {
            // We have a codeSystem in play
            Element codeSystem = (Element) codeSystemSet.get(0);
            List<Element> concepts = XMLParserUtils.descendantsList(codeSystem, "concept");

            sb.append("<p><b>CodeSystem:</b> " + getFirstNamedChildValue(codeSystem, "system") + "<br />");

            if(concepts.size() > 0) {
                sb.append("<ul>");

                for(int i = 0; i < concepts.size(); i++) {
                    Element concept = concepts.get(i);
                    sb.append("<li>");
                    Element code = XMLParserUtils.descendantsList(concept, "code").get(0);
                    sb.append("<b>code:</b> " + code.getAttribute("value"));
                    sb.append(" ");
                    sb.append("<b>display:</b> " + getFirstNamedChildValue(concept, "display"));

                    for(int j = 0; j < conceptMaps.size(); j++) {
                        Element thisMap = conceptMaps.get(j);

                        // Get the name for this mapping...
                        String mapName = getFirstNamedChildValue(thisMap, "name");

                        // Get the target reference for it...
                        Element targetReferenceElement = XMLParserUtils.descendantsList(thisMap, "targetReference").get(0);
                        List<Element> mapItems = XMLParserUtils.descendantsList(thisMap, "element");
                        sb.append("<ul>");
                        for(int k = 0; k< mapItems.size(); k++) {
                            Element mapItem = mapItems.get(k);
                            String mapItemCode = getFirstNamedChildValue(mapItem, "code");

                            // Finally!!!
                            if(mapItemCode.equals(code.getAttribute("value"))) {
                                // We have a mapped value
                                List<Element> targetList = XMLParserUtils.descendantsList(mapItem, "target");
                                for(int l = 0; l < targetList.size(); l++) {
                                    Element target = targetList.get(l);

                                    sb.append("<li>");
                                    
                                    // Add the target code it maps to...
                                    sb.append("<b>maps to:</b> " + getFirstNamedChildValue(target, "code"));

                                    // Now add how it is mapped
                                    sb.append(" (" + getFirstNamedChildValue(target, "equivalence") + ") ");

                                    // Now add in which mapping
                                    sb.append(" in <a href='" + getFirstNamedChildValue(targetReferenceElement, "reference") + "'>" + mapName + "</a>");
                                    
                                    sb.append("</li>");
                                }
                            }
                        }
                        sb.append("</ul>");
                    }
                    sb.append("</li>");
                }
                sb.append("</ul>");
            }
            sb.append("</p>");
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Here we handle an expansion section">
        List<Element> expansionList = XMLParserUtils.descendantsList(thisDoc, "expansion");
        if(expansionList.size() == 1) {
            Element expansion = expansionList.get(0);
            sb.append("<p><h4>Expansion</h4>");
            sb.append("<b>NB: Expansions are not fully catered for in generating the narrative section</b></ br>");
            sb.append("identifier: " + getFirstNamedChildValue(expansion, "identifier"));
            sb.append("timestamp: " + getFirstNamedChildValue(expansion, "timestamp"));

            List<Element> totalList = XMLParserUtils.descendantsList(expansion, "total");
            if(totalList.size() == 1) {
                Element totalEle = totalList.get(0);
                sb.append("<b>total: </b>" + totalEle.getAttribute("value") + "<br />");
            }
            sb.append("</p>");
        }
        //</editor-fold>

        sb.append("</div>\n");
        return sb.toString();
    }
}
