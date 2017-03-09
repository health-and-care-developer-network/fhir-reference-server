package uk.nhs.fhir.makehtml.old;

import static uk.nhs.fhir.makehtml.old.XMLParserUtils.getFirstNamedChildValue;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.util.IteratorIterable;

public class OperationDefinitionHTMLMakerOLD extends HTMLMakerOLD {

	/**
     * Method to generate the narrative section describing an OperationDefinition
     *
     * @param thisDoc   The XML Document as an org.w3c.dom.Document
     * @return          String holding an xhtml div
	 * @throws ParserConfigurationException 
     */
	@Override
	public String makeHTML(Document thisDoc) throws ParserConfigurationException {
        StringBuilder sb = new StringBuilder();

        Element root = thisDoc.getRootElement();
        
        sb.append("<div style='font-family: sans-serif;' xmlns='http://www.w3.org/1999/xhtml'>\n");
        // Here's where we need to do the magic...

        sb.append("<table style='font-family: sans-serif;'><tr><th>Name</th><th>Value</th></tr>");

        /*sb.append(makeOpDefRow(root, "url", "URL", "Logical URL to reference this operation definition"));
        sb.append(makeOpDefRow(root, "version", "Version", "Logical id for this version of the operation definition"));
        sb.append(makeOpDefRow(root, "name", "Name", "Informal name for this operation"));
        sb.append(makeOpDefRow(root, "status", "Status", "draft | active | retired"));
        sb.append(makeOpDefRow(root, "kind", "Kind", "operation | query"));
        sb.append(makeOpDefRow(root, "experimental", "Experimental", "If for testing purposes, not real usage"));
        sb.append(makeOpDefRow(root, "publisher", "Publisher", "Name of the publisher (Organization or individual)"));
        */
        IteratorIterable<Element> contacts = root.getDescendants(new ElementFilter("contact"));
        if(contacts.hasNext()) {
            sb.append("<tr><td colspan='2' style='border-bottom: 1px solid #ddd;'></td></tr>");
            sb.append("<tr><td colspan='2'><b>Contacts</b></td></tr>");

            for(Element contact : contacts) {
				sb.append(makeOpDefRow(contact, "name", "Name", "Name of a individual to contact"));
				for (Element telecom : contact.getDescendants(new ElementFilter("telecom"))) {
                    sb.append(makeOpDefRow(telecom, "system", "Type", "phone | fax | email | pager | other"));
                    sb.append(makeOpDefRow(telecom, "value", "Value", "The actual contact point details"));
                    sb.append(makeOpDefRow(telecom, "use", "Use type", "home | work | temp | old | mobile - purpose of this contact point"));
                }
            }
            sb.append("<tr><td colspan='2' style='border-bottom: 1px solid #ddd;'></td></tr>");
        }

        sb.append(makeOpDefRow(root, "date", "Date", "Date for this version of the operation definition"));
        sb.append(makeOpDefRow(root, "description", "Description", "Natural language description of the operation"));
        sb.append(makeOpDefRow(root, "requirements", "Requirements", "Why is this needed?"));
        sb.append(makeOpDefRow(root, "idempotent", "Is idempotent", "Whether content is unchanged by operation"));
        sb.append(makeOpDefRow(root, "code", "Code", "Name used to invoke the operation"));
        sb.append(makeOpDefRow(root, "notes", "Notes", "Additional information about use"));
        sb.append(makeOpDefRow(root, "base", "Base", "Marks this as a profile of the base"));
        sb.append(makeOpDefRow(root, "system", "System", "Invoke at the system level?"));
        
        // Here we need to show multiple reference types.
        IteratorIterable<Element> types = root.getDescendants(new ElementFilter("type"));
        if (types.hasNext()) {
            sb.append("<tr><td valign='top'><span title='Invoke at resource level for these type'>Type</span></td><td>");
            for (Element type : types) {
            	sb.append(decorateTypeName(type.getAttributeValue("value")) + "<br />");
            }
            sb.append("</td></tr>");
        }
        
        // sb.append(makeOpDefRow(root, "type", "Type"));
        
        sb.append(makeOpDefRow(root, "instance", "Instance", "Invoke on an instance?"));


        sb.append("</table>");

        // Now we iterate through the Parameters
        IteratorIterable<Element> parameters = root.getDescendants(new ElementFilter("parameter"));
        
        if (parameters.hasNext()){
            sb.append("<table style='font-family: sans-serif;'><tr><th colspan='2'>Parameters</th></tr>");
            for (Element parameter : parameters) {
                sb.append(makeParameterItem(parameter));
            }
            sb.append("</table>");
        }

        sb.append("</div>\n");
        return sb.toString();
    }

    protected String makeOpDefRow(Element parentNode,String nodeName) {
        String resultString = "";
        String value = getFirstNamedChildValue(parentNode, nodeName);
        if(value != null) {
            resultString = ("<tr><td>" + nodeName + "</td><td>" + value + "</td></tr>");
        }
        return resultString;
    }
    
    protected String makeOpDefRow(Element parentNode,String nodeName, String title) {
        String resultString = "";
        String value = getFirstNamedChildValue(parentNode, nodeName);
        if(value != null) {
            resultString = ("<tr><td>" + title + "</td><td>" + value + "</td></tr>");
        }
        return resultString;
    }

    protected String makeOpDefRow(Element parentNode,String nodeName, String title, String hover) {
        String resultString = "";
        String value = getFirstNamedChildValue(parentNode, nodeName);
        if(value != null) {
            resultString = ("<tr><td><span title='" + hover + "'>" + title + "</span></td><td>" + value + "</td></tr>");
        }
        return resultString;
    }


    /**
     * Makes a set of table rows based on a passed Parameter item.
     *
     * Should look roughly like this:
     *
     * ----------------------------------------------
     * Parameter Name                               |
     * ----------------------------------------------
     * Definition  | Definition text                |
     * ----------------------------------------------
     * Control     | Control text                   |
     * ----------------------------------------------
     * Type        | data type (as a link?)         |
     * ----------------------------------------------
     * Requirements| Req text, could be big!        |
     * ----------------------------------------------
     * Summary     | Included in summaries?         |
     * ----------------------------------------------
     * Comments    | Comments text                  |
     * ----------------------------------------------
     *
     * @param parameter
     * @return
     */
    protected String makeParameterItem(Element parameter) {
        StringBuilder sb = new StringBuilder();

        sb.append("<tr><td colspan='2' bgcolor='#f0f0f0'><span title='Name in Parameters.parameter.name or in URL'>");
        sb.append(getFirstNamedChildValue(parameter, "name"));
        sb.append("</span></td></tr>");

        sb.append(makeOpDefRow(parameter, "documentation", "Documentation", "Description of meaning/use"));
        sb.append(makeOpDefRow(parameter, "use", "Use", "in | out"));
        sb.append(makeOpDefRow(parameter, "min", "Min", "Minimum Cardinality"));
        sb.append(makeOpDefRow(parameter, "max", "Max", "Maximum Cardinality (a number or *)"));

        String value = getFirstNamedChildValue(parameter, "type");
        if(value != null) {
            sb.append("<tr><td><span title='What type this parameter has'>Type</span></td><td>" + decorateTypeName(value) + "</td></tr>");
        }
        return sb.toString();
    }
}
