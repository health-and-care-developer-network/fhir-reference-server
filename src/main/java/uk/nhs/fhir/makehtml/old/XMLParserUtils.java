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
package uk.nhs.fhir.makehtml.old;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Text;
import org.jdom2.filter.ElementFilter;
import org.jdom2.filter.Filters;
import org.jdom2.util.IteratorIterable;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import com.google.common.collect.Lists;

/**
 * Utility methods for parsing XML content
 * @author Adam Hatherly
 */
public class XMLParserUtils {
    private static final Logger LOG = Logger.getLogger(XMLParserUtils.class.getName());
    private static final XPathFactory factory = XPathFactory.instance();
    
    /**
     * Gets the name of an element from the XML Element
     * 
     * @param item the <element>...</element> section from a StructureDefinition
     * @return String holding the name
     */
    protected static String getElementName(Element item) {
        Element node = item.getDescendants(new ElementFilter("path")).next();
        if(node != null)
            return node.getAttributeValue("value");
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
        Element node = element.getDescendants(new ElementFilter("min")).next();
        if (node != null) {
            min = node.getAttributeValue("value");
            node = element.getDescendants(new ElementFilter("max")).next();
            if (node != null) {
                max = node.getAttributeValue("value");
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
        
        //Element nameValue = (Element) element.getElementsByTagName("path").item(0);
        
        IteratorIterable<Element> typesListIterator = element.getDescendants(new ElementFilter("type"));
        List<Element> typesList = Lists.newArrayList();
        while (typesListIterator.hasNext()) {
        	typesList.add(typesListIterator.next());
        }
        
        if (typesList.isEmpty()) {
        	// We have no types at all - could be a slice
        	// TODO: Handle slicing
        } else if(typesList.size() > 1) {
            // check if any are of type Reference...
            //boolean aReference = false;
            for (int i = 0; i < typesList.size(); i++) {
                Element node = typesList.get(i);

                // Here we get the contained element called 'code'
                Element thisCode = (Element) node.getDescendants(new ElementFilter("code")).next();
                String thisType = thisCode.getAttributeValue("value");
                
                // If it's a new type we don't already have, then add it to our list of types
                if (!types.contains(thisType)) {
                    types.add(thisType);
                }
                if (thisType.equals("Reference")) {
                    // If it's a Reference, get the profile for it and add to our list of Profiles
                    IteratorIterable<Element> descendants = node.getDescendants(new ElementFilter("profile"));
                    if (descendants.hasNext()) {
						Element profileElement = descendants.next();
                        String prof = profileElement.getAttributeValue("value");
                        profiles.add(prof);
                    }
                }
            }
            
        } else {
        	// Simple mode where we only have on type element!
            Element typeNode = typesList.get(0);
            Element theCodeElement = typeNode.getDescendants(new ElementFilter("code")).next();
            String thisType = theCodeElement.getAttributeValue("value");
            types.add(thisType);
            if (thisType.equals("Reference")) {
	            IteratorIterable<Element> descendants = typeNode.getDescendants(new ElementFilter("profile"));
                if (descendants.hasNext()) {
					Element profileNode = descendants.next();
                	String profileName = profileNode.getAttributeValue("value");
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
    protected static List<String> getElementTypeList(Element element) {
        List<String> types = new ArrayList<String>();
        for (Element node : element.getDescendants(new ElementFilter("type"))) {
            Element subNode = (Element) node.getDescendants(new ElementFilter("code")).next();
            types.add(subNode.getAttributeValue("value"));
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

        IteratorIterable<Element> summaryList = element.getDescendants(new ElementFilter("isSummary"));
        if(summaryList.hasNext()) {
            Element summary = (Element) summaryList.next();
            if(summary.getAttributeValue("value").equals("true")) {
                flags = flags + "<span xmlns=\"http://www.w3.org/1999/xhtml\" title=\"This element is included in summaries\">&#931;</span>\n";
            }
        }

        IteratorIterable<Element> conditionList = element.getDescendants(new ElementFilter("condition"));
        if(conditionList.hasNext()) {
            Element condition = (Element) conditionList.next();
            if(condition.getAttributeValue("value").equals("true")) {
                flags = flags + "<span xmlns=\"http://www.w3.org/1999/xhtml\" title=\"This element has or is affected by some invariants\">I</span>\n";
            }
        }

        IteratorIterable<Element> modifierList = element.getDescendants(new ElementFilter("isModifier"));
        if(modifierList.hasNext()) {
            Element modifier = (Element) modifierList.next();
            if(modifier.getAttributeValue("value").equals("true")) {
                flags = flags + "<span xmlns=\"http://www.w3.org/1999/xhtml\" title=\"This element is a modifier element\">?!</span>";
            }
        }
        return flags;
    }

    protected static String getTitle(Element element) {
        String title = "";
        IteratorIterable<Element> titleList = element.getDescendants(new ElementFilter("short"));
        if(titleList.hasNext()) {
            Element subNode = (Element) titleList.next();
            title = escapeHtml4(subNode.getAttributeValue("value"));
        }
        return title;
    }

    protected static String getDescription(Element element) {
        String description = "";
        IteratorIterable<Element> descList = element.getDescendants(new ElementFilter("definition"));
        if(descList.hasNext()) {
            Element subNode = (Element) descList.next();
            description = escapeHtml4(subNode.getAttributeValue("value"));
        }
        return description;
    }
    
    protected static String getReferenceTypes(Element element) {
        String result = "";
        ArrayList<String> profiles = new ArrayList<String>();
        for (Element atype : element.getDescendants(new ElementFilter("type"))) {
            IteratorIterable<Element> descendants = atype.getDescendants(new ElementFilter("profile"));
            if (descendants.hasNext()) {
            	Element profileName = descendants.next();
                String attrName = profileName.getAttributeValue("value");
                if(attrName != null) {
                    profiles.add(profileName.getAttributeValue("value"));
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
    	Element atype = element.getDescendants(new ElementFilter("type")).next();
        Element profileName = atype.getDescendants(new ElementFilter("profile")).next();
        String result = "";
        try {
	        result = profileName.getAttributeValue("value");
	        result = decorateProfileName(result);
        } catch (NullPointerException npe) {
        	LOG.severe("Unable to get value attribute for element: " + element.getName() + " with profile: " + profileName);
        }
        return result;
    }

    private static String decorateProfileName(String profileName) {
        String result = "<a href='" + profileName + "'>";
        result = result + profileName.substring(profileName.lastIndexOf("/")+1);
        result = result + "</a>";
        return result;
    }
    
    protected static String getValueSetChildValue(Document theDoc, String childTag) {
	    try {
	        XPathExpression<Text> xpath = factory.compile("/ValueSet/" + childTag + "/@value", Filters.textOnly());
	        Text publisher = xpath.evaluateFirst(theDoc);
	        return publisher.getText();
	    } catch (IllegalStateException | IllegalArgumentException e) {
	    	Logger.getLogger(XMLParserUtils.class.getName()).log(Level.SEVERE, null, e);
	    	return null;
	    }
    }
    
    /**
     * Uses xpath to get the name from a ValueSet
     * 
     * @param theDoc
     * @return 
     */
    protected static String getValueSetName(Document theDoc) {
        return getValueSetChildValue(theDoc, "name");
    }
    
    /**
     * Use xpath to get the url of a valueset
     * 
     * @param theDoc
     * @return 
     */
    protected static String getValueSetURL(Document theDoc) {
        return getValueSetChildValue(theDoc, "url");
    }

    /**
     * Use xpath to get the publisher of a valueset
     * 
     * @param theDoc
     * @return 
     */
    protected static String getValueSetPublisher(Document theDoc) {
        return getValueSetChildValue(theDoc, "publisher");
    }
    
    /**
     * Use xpath to get the status of a valueset
     * 
     * @param theDoc
     * @return 
     */
    protected static String getValueSetStatus(Document theDoc) {
        return getValueSetChildValue(theDoc, "status");   
    }

    /**
     * Use xpath to get the version of a valueset
     * 
     * @param theDoc
     * @return 
     */
    protected static String getValueSetVersion(Document theDoc) {
        return getValueSetChildValue(theDoc, "version");       
    }

    /**
     * Gets the value attribute of a org.w3c.dom.Node
     * 
     * @param thisOne   An Element
     * @return          String or null
     */
    protected static String getElementValue(Element thisOne) {
    	return thisOne == null ? null : thisOne.getAttributeValue("value");
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
    public static String getFirstNamedChildValue(Element thisOne, String name) {
        String theValue = null;
        if(thisOne != null) {
            Element thisElement = (Element) thisOne;
            Element theElement = thisElement.getDescendants(new ElementFilter(name)).next();
            theValue = getElementValue(theElement);
        }        
        return theValue;
    }

    /**
     * Loops through any extensions it finds, looking for one with the specified URL, then
     * returns the value of the specified element in that extension
     * @param node Parent node containing extensions
     * @param url URL of the extension to look for
     * @param valueElement Value element to take from the matched extension
     * @return Value of the specified element in the specified extension, or null if not found
     */
    public static String getExtensionValue(Element node, String url, String valueElement) {
    	for (Element extensionElement : node.getDescendants(new ElementFilter("extension"))) {
    		String extensionUrl = extensionElement.getAttributeValue("url");
    		if (extensionUrl.equals(url)) {
    			return getFirstNamedChildValue(extensionElement, valueElement);
    		}
    	}
    	
		return null;
    }

	public static List<Element> descendantsList(Document thisDoc, String tagName) {
		List<Element> descendants = Lists.newArrayList();
        for (Element e : thisDoc.getDescendants(new ElementFilter(tagName))) {
        	descendants.add(e);
        }
		return descendants;
	}

	public static List<Element> descendantsList(Element element, String tagName) {
		List<Element> descendants = Lists.newArrayList();
        for (Element e : element.getDescendants(new ElementFilter(tagName))) {
        	descendants.add(e);
        }
		return descendants;
	}
}
