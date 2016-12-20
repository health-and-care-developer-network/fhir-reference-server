package uk.nhs.fhir;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Level;
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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
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
                                    Node resource = resourceContents.item(0);
                                    Node resourceContentElement = (Node) resource.getChildNodes().item(1);
                                    //String peekaboo = nodeToString(resource);                   // Added to see what's in the parent element during debugging, not used.
                                    result = nodeToString(resourceContentElement);    // Get the entire contents of the specified Element.
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
                // Here can we assume we've got JSON, is this check sstrong enough??
            }
        }
        return result;
    }
    
    public static String getPassedResource(StringBuffer inputtext) {
        String result = null;
        try {
            String input = inputtext.toString();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(input));
            Document document = docBuilder.parse(is);
            
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("/Parameters/parameter/resource/element()/node()");
            result = expr.evaluate(document);
                    
                    
        } catch (XPathExpressionException ex) {
            LOG.severe("XPathExpressionException: " + ex.getMessage());
            Logger.getLogger(ResourceSeparator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            LOG.severe("ParserConfigurationException: " + ex.getMessage());
        } catch (SAXException ex) {
            Logger.getLogger(ResourceSeparator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ResourceSeparator.class.getName()).log(Level.SEVERE, null, ex);
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
