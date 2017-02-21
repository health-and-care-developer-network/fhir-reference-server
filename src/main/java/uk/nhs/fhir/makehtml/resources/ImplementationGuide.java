package uk.nhs.fhir.makehtml.resources;

import static uk.nhs.fhir.makehtml.XMLParserUtils.getFirstNamedChildValue;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ImplementationGuide {
    /**
     * Make the html narrative section for the ImplementationGuide described in this XML
     *
     * @param thisDoc   The XML Document as an org.w3c.dom.Document
     *
     * @return          Valid xhtml fully describing the ImplementationGuide
     */
    public static String makeHTMLForImplementationGuide(Document thisDoc, File folder) {

        StringBuilder sb = new StringBuilder();

        Element root = (Element) thisDoc.getFirstChild();

        sb.append("<div style='font-family: sans-serif;' xmlns='http://www.w3.org/1999/xhtml'>\n");

        
        // TODO: Load in the relevant markdown file and inject it here
        
        
        // TODO: Update the below for ImplementationGuide!
        
        sb.append("<table style='font-family: sans-serif;'><tr><th>Name</th><th>Value</th></tr>");
        
        NodeList composeSet = thisDoc.getElementsByTagName("compose");
        if(composeSet.getLength() > 0) {
            sb.append("<h4>Composed from</h4>");

            Element composeElement = (Element) composeSet.item(0);

            // The compose can be one or more of Import, Inlude and Exclude sections
            NodeList composeImports = composeElement.getElementsByTagName("import");
            NodeList composeIncludes = composeElement.getElementsByTagName("include");
            NodeList composeExcludes = composeElement.getElementsByTagName("exclude");

            // Imports is dead easy...
            for(int i = 0; i < composeImports.getLength(); i++) {
                Element importRef = (Element) composeImports.item(i);
                sb.append("<p><b>Import:</b> " + importRef.getAttribute("value") + "<br /></p>");
            }

            // Includes is more tricky...
            for(int i = 0; i < composeIncludes.getLength(); i++) {
                Element includeRef = (Element) composeIncludes.item(i);

                sb.append("<p><table><tr><td><b>Code System:</b></td><td>" + getFirstNamedChildValue(includeRef, "system") + "</td></tr>");

                NodeList filterList = includeRef.getElementsByTagName("filter");
                if (filterList.getLength()>0) {
                	sb.append("<tr><td colspan='2'><b>Filters:</b></td></tr>");
                }
                for(int j = 0; j < filterList.getLength(); j++) {
                    Element theFilter = (Element) filterList.item(j);
                    sb.append("<tr><td>Property:</td><td>" + getFirstNamedChildValue(theFilter, "property") + "</td></tr>");
                    sb.append("<tr><td>Operation:</td><td>" + getFirstNamedChildValue(theFilter, "op") + "</td></tr>");
                    sb.append("<tr><td>Value:</td><td>" + getFirstNamedChildValue(theFilter, "value") + "</td></tr>");
                }
                
                NodeList includeList = includeRef.getElementsByTagName("concept");
                if (includeList.getLength()>0) {
                	sb.append("<tr><td colspan='2'><b>Includes:</b></td></tr>");
                }
                for(int j = 0; j < includeList.getLength(); j++) {
                    Element theInclude = (Element) includeList.item(j);
                    sb.append("<tr><td colspan='2'><li>");
                    sb.append("<b>code:</b> " + getFirstNamedChildValue(theInclude, "code"));
                    sb.append(": ");
                    sb.append("<b>display:</b> " + getFirstNamedChildValue(theInclude, "display"));
                    sb.append("</li></td></tr>");
                    
                }
                
                sb.append("</table></p>");
            }

            // Excludes is identical to Includes...
            for(int i = 0; i < composeExcludes.getLength(); i++) {
                Element excludeRef = (Element) composeExcludes.item(i);

                sb.append("<p><table><tr><td><b>Exclude:</b></td><td>" + getFirstNamedChildValue(excludeRef, "system") + "</td></tr>");

                NodeList filterList = excludeRef.getElementsByTagName("filter");
                for(int j = 0; j < filterList.getLength(); j++) {
                    Element theFilter = (Element) filterList.item(j);

                    sb.append("<tr><td>Property:</td><td>" + getFirstNamedChildValue(theFilter, "property") + "</td></tr>");
                    sb.append("<tr><td>Operation:</td><td>" + getFirstNamedChildValue(theFilter, "op") + "</td></tr>");
                    sb.append("<tr><td>Value:</td><td>" + getFirstNamedChildValue(theFilter, "value") + "</td></tr>");
                }
                sb.append("</table></p>");
            }

        }

        NodeList codeSystemSet = thisDoc.getElementsByTagName("codeSystem");

        NodeList conceptMaps = thisDoc.getElementsByTagName("ConceptMap");

        if(codeSystemSet.getLength() > 0) {
            // We have a codeSystem in play
            Element codeSystem = (Element) codeSystemSet.item(0);
            NodeList concepts = codeSystem.getElementsByTagName("concept");

            sb.append("<p><b>CodeSystem:</b> " + getFirstNamedChildValue(codeSystem, "system") + "<br />");

            if(concepts.getLength() > 0) {
                sb.append("<ul>");

                for(int i = 0; i < concepts.getLength(); i++) {
                    Element concept = (Element) concepts.item(i);
                    sb.append("<li>");
                    Element code = (Element) concept.getElementsByTagName("code").item(0);
                    sb.append("<b>code:</b> " + code.getAttribute("value"));
                    sb.append(" ");
                    sb.append("<b>display:</b> " + getFirstNamedChildValue(concept, "display"));

                    for(int j = 0; j < conceptMaps.getLength(); j++) {
                        Element thisMap = (Element) conceptMaps.item(j);

                        // Get the name for this mapping...
                        String mapName = getFirstNamedChildValue(thisMap, "name");

                        // Get the target reference for it...
                        Element targetReferenceElement = (Element) thisMap.getElementsByTagName("targetReference").item(0);
                        NodeList mapItems = thisMap.getElementsByTagName("element");
                        sb.append("<ul>");
                        for(int k = 0; k< mapItems.getLength(); k++) {
                            Element mapItem = (Element) mapItems.item(k);
                            String mapItemCode = getFirstNamedChildValue(mapItem, "code");

                            // Finally!!!
                            if(mapItemCode.equals(code.getAttribute("value"))) {
                                // We have a mapped value
                                NodeList targetList = mapItem.getElementsByTagName("target");
                                for(int l = 0; l < targetList.getLength(); l++) {
                                    Element target = (Element) targetList.item(l);

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

        NodeList expansionList = thisDoc.getElementsByTagName("expansion");
        if(expansionList.getLength() == 1) {
            Element expansion = (Element) expansionList.item(0);
            sb.append("<p><h4>Expansion</h4>");
            sb.append("<b>NB: Expansions are not fully catered for in generating the narrative section</b></ br>");
            sb.append("identifier: " + getFirstNamedChildValue(expansion, "identifier"));
            sb.append("timestamp: " + getFirstNamedChildValue(expansion, "timestamp"));

            NodeList totalList = expansion.getElementsByTagName("total");
            if(totalList.getLength() == 1) {
                Element totalEle = (Element) totalList.item(0);
                sb.append("<b>total: </b>" + totalEle.getAttribute("value") + "<br />");
            }
            sb.append("</p>");
        }

        sb.append("</div>\n");
        return sb.toString();
    }

}
