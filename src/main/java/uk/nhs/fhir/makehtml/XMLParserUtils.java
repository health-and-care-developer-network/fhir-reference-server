/*
 * Copyright (C) 2016 Health and Social Care Information Centre.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.nhs.fhir.makehtml;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Utility methods for parsing XML content
 * @author Adam Hatherly
 */
public class XMLParserUtils {
    private static final Logger LOG = Logger.getLogger(XMLParserUtils.class.getName());
    
    /**
     * Gets the name of an element from the XML Element
     * 
     * @param item the <element>...</element> section from a StructureDefinition
     * @return String holding the name
     */
    protected static String getElementName(Element item) {
        Element node = (Element) item.getElementsByTagName("path").item(0);
        if(node != null)
            return node.getAttribute("value");
        else {
            LOG.warning("getElementName() couldn't find a path element for an element.");
            return "";
        }
    }

    /**
     * Gets the name of an element from the XML Element
     * 
     * @param item the <element>...</element> section from a StructureDefinition
     * @return 
     */
    protected static String getElementCardinality(Element element) {
        String min = "";
        String max = "";
        String cardinality;
        Element node = (Element) element.getElementsByTagName("min").item(0);
        if(node != null) {
            min = node.getAttribute("value");
            node = (Element) element.getElementsByTagName("max").item(0);
            if(node != null) {
                max = node.getAttribute("value");
            }
            cardinality = min + ".." + max;
        } else {
            cardinality = "???";
        }
        return cardinality;
    }

    /**
     * Gets the Type Name of an element from the XML Element
     * 
     * @param item the <element>...</element> section from a StructureDefinition
     * @return 
     */
    protected static String getElementTypeName(Element element) {
    	String typeName = null;
        ArrayList<String> profiles = new ArrayList<String>();
        ArrayList<String> types = new ArrayList<String>();
        
        Element nameValue = (Element) element.getElementsByTagName("path").item(0);
        
        NodeList typesList = element.getElementsByTagName("type");
        if (typesList.getLength() == 0) {
        	// We have no types at all - could be a slice
        	// TODO: Handle slicing
        } else if(typesList.getLength() > 1) {
            // check if any are of type Reference...
            boolean aReference = false;
            for(int i = 0; i < typesList.getLength(); i++) {
                Element node = (Element) typesList.item(i);

                // Here we get the contained element called 'code'
                Element thisCode = (Element) node.getElementsByTagName("code").item(0);
                String thisType = thisCode.getAttribute("value");
                
                // If it's a new type we don't already have, then add it to our list of types
                if(!types.contains(thisType)) {
                    types.add(thisType);
                }
                if(thisType.equals("Reference")) {
                    // If it's a Reference, get the profile for it and add to our list of Profiles
                    Element profileElement = (Element) node.getElementsByTagName("profile").item(0);
                    if(profileElement != null) {
                        String prof = profileElement.getAttribute("value");
                        profiles.add(prof);
                    }
                    aReference = true;
                }
            }
            
        } else {
        	// Simple mode where we only have on type element!
            Element typeNode = (Element) typesList.item(0);
            Element theCodeElement = (Element) typeNode.getElementsByTagName("code").item(0);
            String thisType = theCodeElement.getAttribute("value");
            types.add(thisType);
            if(thisType.equals("Reference")) {
                Element profileNode = (Element) typeNode.getElementsByTagName("profile").item(0);
                if (profileNode != null) {
                	String profileName = profileNode.getAttribute("value");
                	profiles.add(profileName);
                }
            }
        }

        // TODO: Incorporate the type finding and decorating in here.
        for(String type : types) {
            if (typeName != null) {
            	typeName = typeName + " | " + type;
            } else {
            	typeName = type;
            }
            if(type.equals("Reference")) {
                if(profiles.size() > 0) {
                    typeName = "<a href='https://www.hl7.org/fhir/references.html'>Reference</a> ( ";
                    for(int i = 0; i < profiles.size() - 1; i++) {
                        typeName = typeName + decorateProfileName(profiles.get(i)) + " | ";
                    }
                    typeName = typeName + decorateProfileName(profiles.get(profiles.size() - 1)) + " ) ";
                } else {
                    typeName = "<a href='https://www.hl7.org/fhir/references.html'>Reference</a>";
                }
            }
        }   
        return typeName;
    }

    /**
     * Gets the Type Names of an element from the XML Element (which has multiple types)
     * 
     * @param item the <element>...</element> section from a StructureDefinition
     * @return 
     */
    protected static ArrayList<String> getElementTypeList(Element element) {
        ArrayList<String> types = new ArrayList<String>();
        NodeList typesList = element.getElementsByTagName("type");
        for(int i = 0; i < typesList.getLength(); i++) {
            Element node = (Element) typesList.item(i);
            Element subNode = (Element) node.getElementsByTagName("code").item(0);
            types.add(subNode.getAttribute("value"));
        }
        return types;
    }
    
    /**
     * Gets the Flags of an element from the XML Element
     * 
     * @param item the <element>...</element> section from a StructureDefinition
     * @return 
     */
    protected static String getFlags(Element element) {
        String flags = "";

        NodeList summaryList = element.getElementsByTagName("isSummary");
        if(summaryList.getLength() > 0) {
            Element summary = (Element) summaryList.item(0);
            if(summary.getAttribute("value").equals("true")) {
                flags = flags + "<span xmlns=\"http://www.w3.org/1999/xhtml\" title=\"This element is included in summaries\">&#931;</span>\n";
            }
        }

        NodeList conditionList = element.getElementsByTagName("condition");
        if(conditionList.getLength() > 0) {
            Element condition = (Element) conditionList.item(0);
            if(condition.getAttribute("value").equals("true")) {
                flags = flags + "<span xmlns=\"http://www.w3.org/1999/xhtml\" title=\"This element has or is affected by some invariants\">I</span>\n";
            }
        }

        NodeList modifierList = element.getElementsByTagName("isModifier");
        if(modifierList.getLength() > 0) {
            Element modifier = (Element) modifierList.item(0);
            if(modifier.getAttribute("value").equals("true")) {
                flags = flags + "<span xmlns=\"http://www.w3.org/1999/xhtml\" title=\"This element is a modifier element\">?!</span>";
            }
        }
        return flags;
    }

    protected static String getTitle(Element element) {
        String title = "";
        NodeList titleList = element.getElementsByTagName("short");
        if(titleList.getLength() > 0) {
            Element subNode = (Element) titleList.item(0);
            title = escapeHtml4(subNode.getAttribute("value"));
        }
        return title;
    }

    protected static String getDescription(Element element) {
        String description = "";
        NodeList descList = element.getElementsByTagName("definition");
        if(descList.getLength() > 0) {
            Element subNode = (Element) descList.item(0);
            description = escapeHtml4(subNode.getAttribute("value"));
        }
        return description;
    }
    
    protected static String getReferenceTypes(Element element) {
        String result = "";
        ArrayList<String> profiles = new ArrayList<String>();
        NodeList typesList = element.getElementsByTagName("type");
        for(int i = 0; i < typesList.getLength(); i++) {
            Element atype = (Element) typesList.item(i);
            Element profileName = (Element) atype.getElementsByTagName("profile").item(0);
            if(profileName != null) {
                String attrName = profileName.getAttribute("value");
                if(attrName != null) {
                    profiles.add(profileName.getAttribute("value"));
                } else {
                    LOG.warning("Profile type for this reference has no type name");
                }
            } else {
                LOG.warning("No Profile type found for this reference");
            }
        }
        
        result = "<a href='https://www.hl7.org/fhir/references.html'>Reference</a>";
        
        if(profiles.size() > 0) {
            result = result + "(";
            for(int i = 0; i < profiles.size()-1; i++) {
                result = result + decorateProfileName(profiles.get(i)) + " | ";
            }
            result = result + decorateProfileName(profiles.get(profiles.size()-1));
            result = result + ")";
        }
        return result;
    }

    protected static String getQuantityType(Element element) {
        NodeList typesList = element.getElementsByTagName("type");
        Element atype = (Element) typesList.item(0);
        Element profileName = (Element) atype.getElementsByTagName("profile").item(0);
        String result = "";
        try {
	        result = profileName.getAttribute("value");
	        result = decorateProfileName(result);
        } catch (NullPointerException npe) {
        	LOG.severe("Unable to get value attribute for element: " + element.getNodeName() + " with profile: " + profileName);
        }
        return result;
    }

    private static String decorateProfileName(String profileName) {
        String result = "<a href='" + profileName + "'>";
        result = result + profileName.substring(profileName.lastIndexOf("/")+1);
        result = result + "</a>";
        return result;
    }
    
    /**
     * Uses xpath to get the name from a ValueSet
     * 
     * @param theDoc
     * @return 
     */
    protected static String getValueSetName(Document theDoc) {
        String name = null;
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        try {
            XPathExpression expr = xpath.compile("/ValueSet/name/@value");
            name = expr.evaluate(theDoc);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(XMLParserUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return name;
    }
    
    /**
     * Use xpath to get the url of a valueset
     * 
     * @param theDoc
     * @return 
     */
    protected static String getValueSetURL(Document theDoc) {
        String url = null;
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        try {
            XPathExpression expr = xpath.compile("/ValueSet/url/@value");
            url = expr.evaluate(theDoc);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(XMLParserUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return url;       
    }

    /**
     * Use xpath to get the publisher of a valueset
     * 
     * @param theDoc
     * @return 
     */
    protected static String getValueSetPublisher(Document theDoc) {
        String publisher = null;
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        try {
            XPathExpression expr = xpath.compile("/ValueSet/publisher/@value");
            publisher = expr.evaluate(theDoc);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(XMLParserUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return publisher;       
    }
    
    /**
     * Use xpath to get the status of a valueset
     * 
     * @param theDoc
     * @return 
     */
    protected static String getValueSetStatus(Document theDoc) {
        String status = null;
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        try {
            XPathExpression expr = xpath.compile("/ValueSet/status/@value");
            status = expr.evaluate(theDoc);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(XMLParserUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;       
    }

    /**
     * Use xpath to get the version of a valueset
     * 
     * @param theDoc
     * @return 
     */
    protected static String getValueSetVersion(Document theDoc) {
        String version = null;
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        try {
            XPathExpression expr = xpath.compile("/ValueSet/version/@value");
            version = expr.evaluate(theDoc);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(XMLParserUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return version;       
    }

    /**
     * Gets the value attribute of a org.w3c.dom.Node
     * 
     * @param thisOne   A org.w3c.dom.Node
     * @return          String or null
     */
    protected static String getElementValue(Node thisOne) {
        String theValue = null;
        if(thisOne != null) {
            Element myElement = (Element) thisOne;
            theValue = myElement.getAttribute("value");
        }
        return theValue;
    }
    
    /**
     * Gets the value attribute of the first matching Named child of a given element
     * So for example:
     * 
     *   <parent>
     *     <firstchild value="not this" />
     *     <secondChild value="but this" />
     *   </parent>
     * 
     *  getFirstNamedChildValue(parent, "secondChild") would return "but this"
     * 
     * @param thisOne
     * @param name
     * @return 
     */
    protected static String getFirstNamedChildValue(Node thisOne, String name) {
        String theValue = null;
        if(thisOne != null) {
            Element thisElement = (Element) thisOne;
            Node theElement = thisElement.getElementsByTagName(name).item(0);
            theValue = getElementValue(theElement);
        }        
        return theValue;
    }
}
