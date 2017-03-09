package uk.nhs.fhir.makehtml.old;

import static uk.nhs.fhir.makehtml.old.XMLParserUtils.getElementName;

import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.util.IteratorIterable;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.Constants;

public abstract class HTMLMakerOLD implements Constants {
	public abstract String makeHTML(Document doc) throws ParserConfigurationException;
	
    /**
     * Method to get a list of the names of the elements which are listed in
     * the differential section of a StructureDefinition, ie those which
     * have been changed.
     *
     * @param document A org.w3c.dom.Document
     * @return Returns an ArrayList of Strings holding the (full dot separated) element names.
     */
    protected ArrayList<String> GetChangedNodes(Document document) {
        ArrayList<String> names = Lists.newArrayList();
        // Get a list of any elements called differential
        IteratorIterable<Element> differential = document.getDescendants(new ElementFilter("differential"));

        if(differential.hasNext()) {
            // Get the first one (there should only be one!
            Element diffNode = differential.next();

            // Get the elements within the differential section
            IteratorIterable<Element> diffElements = diffNode.getDescendants(new ElementFilter("element"));

            while (diffElements.hasNext()) {
                Element diffElement = (Element) diffElements.next();
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
