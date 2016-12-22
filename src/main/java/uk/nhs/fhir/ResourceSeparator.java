package uk.nhs.fhir;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang3.NotImplementedException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author tim.coates@hscic.gov.uk
 */
public class ResourceSeparator {
    private static final Logger LOG = Logger.getLogger(ResourceSeparator.class.getName());
    
    public static String getResource(StringBuffer inputtext) {

        String result = null;
        
        String input = inputtext.toString();
        if(input.startsWith("<Parameters")) {
            try {
                // We've got XML input
                Document document = null;
                DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                InputSource is = new InputSource(new StringReader(input));
                document = docBuilder.parse(is);                
                NodeList parameters = document.getElementsByTagName("parameter");
                
                // Here we've hopefully got a set of parameters, including:
                // mode
                // profile
                // resource
                if(parameters.getLength() != 0) {
                    for(int i = 0; i < parameters.getLength(); i++) {
                        // We iterate through looking for the resource one...
                        Element param = (Element) parameters.item(i);
                        
                        // Each parameter has a sub element called name, we loko for the name for this parameter
                        NodeList nameParts = param.getElementsByTagName("name");
                        if(nameParts.getLength() != 0) {
                            
                            Element nameValue = (Element) nameParts.item(0);
                            String nameVal = nameValue.getAttribute("value"); // We get the value of that name element

                            if(nameVal.equals("resource")) {
                                // Here we've isolated the resource parameter within the Parameters section...    
                                // That element has an inner element called resource, which has the actual Resource as it's inner text
                                NodeList resourceContents = param.getElementsByTagName("resource");
                                if(resourceContents.getLength() != 0) {
                                    Element resource = (Element) resourceContents.item(0);
                                    NodeList resourceContentElements = resource.getElementsByTagName("*");  // Get the first Element the within the <resource> element
                                    result = nodeToString(resourceContentElements.item(0));    // Get the entire contents of the specified Element.
                                }
                            }
                        }
                    }
                }
                
            } catch (ParserConfigurationException ex) {
                LOG.severe("ParserConfigurationException: " + ex.getMessage());
            } catch (SAXException ex) {
                LOG.severe("SAXException: " + ex.getMessage());
            } catch (IOException ex) {
                LOG.severe("IOException: " + ex.getMessage());
            }
        } else {
            if(input.startsWith("{") || input.startsWith("[")) {
                // Here can we assume we've got JSON, is this check strong enough??
                throw new NotImplementedException("Not yet ready to validate JSON resources");
            }
        }
        return result;
    }
    
    private static String nodeToString(Node node) {
        StringWriter buf = new StringWriter();
        try {
            Transformer xform = TransformerFactory.newInstance().newTransformer();
            xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            //xform.setOutputProperty(OutputKeys.INDENT, "no");
            xform.transform(new DOMSource(node), new StreamResult(buf));
        } catch (TransformerException ex) {
            LOG.severe("TransformerException: " + ex.getMessage());
        }
        return (buf.toString());
    }

}
