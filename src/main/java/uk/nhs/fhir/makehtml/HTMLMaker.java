package uk.nhs.fhir.makehtml;

import static uk.nhs.fhir.makehtml.XMLParserUtils.getElementName;

import java.util.ArrayList;
import java.util.Arrays;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class HTMLMaker implements Constants {
	public abstract String makeHTML(Document doc);
	
    /**
     * Method to get a list of the names of the elements which are listed in
     * the differential section of a StructureDefinition, ie those which
     * have been changed.
     *
     * @param document A org.w3c.dom.Document
     * @return Returns an ArrayList of Strings holding the (full dot separated) element names.
     */
    protected ArrayList<String> GetChangedNodes(Document document) {
        ArrayList<String> names = new ArrayList<String>();
        // Get a list of any elements called differential
        NodeList differential = document.getElementsByTagName("differential");

        if(differential.getLength() > 0) {
            // Get the first one (there should only be one!
            Element diffNode = (Element) differential.item(0);

            // Get the elements within the differential section
            NodeList diffElements = diffNode.getElementsByTagName("element");

            for(int i = 0; i < diffElements.getLength(); i++) {
                Element diffElement = (Element) diffElements.item(i);
                if(diffElement != null) {
                    String name = getElementName(diffElement);
                    if(name != null) {
                        names.add(name);
                    }
                }
            }
        }
        return names;
    }

    /**
     * Dress up a type name so it provides a link back to the definition.
     *
     * @param type
     * @return
     */
    public String decorateTypeName(String type) {
        if(type.equals("DomainResource")) {
            return "<a href='https://www.hl7.org/fhir/domainresource.html'>" + type + "</a>";
        }
        if(Arrays.asList(BASERESOURCETYPES).contains(type)) {
            return "<a href='https://www.hl7.org/fhir/datatypes.html#" + type + "'>" + type + "</a>";
        } else {
            if(Arrays.asList(RESOURCETYPES).contains(type)){
                return "<a href='https://www.hl7.org/fhir/" + type.toLowerCase() + ".html'>" + type + "</a>";
            }
            else
                return type;
        }
    }

    public String decorateResourceName(String type) {
        if(Arrays.asList(RESOURCETYPES).contains(type)) {
            return "<a href='https://www.hl7.org/fhir/" + type.toLowerCase() + ".html'>" + type + "</a>";
        }
        else {
            return type;
        }
    }
}
