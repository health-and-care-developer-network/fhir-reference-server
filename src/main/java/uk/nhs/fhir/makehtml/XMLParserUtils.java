package uk.nhs.fhir.makehtml;

import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility methods for parsing XML content
 * @author Adam Hatherly
 */
public class XMLParserUtils {
    protected static String getElementName(Element item) {
        NodeList pathsList = item.getElementsByTagName("path");
        Element node = (Element) pathsList.item(0);
        return node.getAttribute("value");
    }

    protected static String getElementCardinality(Element element) {
        NodeList minList = element.getElementsByTagName("min");
        Element node = (Element) minList.item(0);
        String min = node.getAttribute("value");

        NodeList maxList = element.getElementsByTagName("max");
        node = (Element) maxList.item(0);
        String max = node.getAttribute("value");
        return min + ".." + max;
    }

    protected static String getElementTypeName(Element element) {
        String typeName = null;
        NodeList typesList = element.getElementsByTagName("type");
        
        if(typesList.getLength() > 1) {
            // Multiple types either it's a Reference to one of many types
            // or it truly can be one of many types...
            Element node = (Element) typesList.item(0);
            NodeList codeList = node.getElementsByTagName("code");
            Element subNode = (Element) codeList.item(0);
            if(subNode.getAttribute("value").equals("Reference")) {
                // We now know it's a Reference to one of many types...
                typeName = "Reference";
                
                for(int t = 0; t < typesList.getLength(); t++) {
                    Element typeItem = (Element) typesList.item(t);
                    NodeList codes = node.getElementsByTagName("profile");
                    Element profile = (Element) codes.item(0);
                    String prof = profile.getAttribute("value");
                    typeName = typeName + prof;
                }                
            } else {
                return "Multiple_Type_Choice";
            }
        }
        
        if(typesList.getLength() > 0) {
            Element node = (Element) typesList.item(0);
            NodeList codeList = node.getElementsByTagName("code");
            Element subNode = (Element) codeList.item(0);
            typeName = subNode.getAttribute("value");
        }
        return typeName;
    }

    protected static ArrayList<String> getElementTypeList(Element element) {
        ArrayList<String> types = new ArrayList<String>();
        NodeList typesList = element.getElementsByTagName("type");
        for(int i = 0; i < typesList.getLength(); i++) {
            Element node = (Element) typesList.item(i);
            NodeList codeList = node.getElementsByTagName("code");
            Element subNode = (Element) codeList.item(0);
            types.add(subNode.getAttribute("value"));
        }
        return types;
    }
    
    protected static String getFlags(Element element) {
        String flags = "";

        NodeList summaryList = element.getElementsByTagName("isSummary");
        if(summaryList.getLength() > 0) {
            Element summary = (Element) summaryList.item(0);
            if(summary.getAttribute("value").equals("true")) {
                flags = flags + "<span xmlns=\"http://www.w3.org/1999/xhtml\" title=\"This element is included in summaries\">&Sigma;</span>\n";
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
            title = subNode.getAttribute("value");
        }
        return title;
    }

    protected static String getDescription(Element element) {
        String description = "";
        NodeList descList = element.getElementsByTagName("definition");
        if(descList.getLength() > 0) {
            Element subNode = (Element) descList.item(0);
            description = subNode.getAttribute("value");
        }
        return description;
    }
    
    protected static String getReferenceTypes(Element element) {
        String result = "";
        ArrayList<String> profiles = new ArrayList<String>();
        NodeList typesList = element.getElementsByTagName("type");
        for(int i = 0; i < typesList.getLength(); i++) {
            Element atype = (Element) typesList.item(i);
            NodeList profileList = atype.getElementsByTagName("profile");
            Element profileName = (Element) profileList.item(0);
            profiles.add(profileName.getAttribute("value"));
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
        NodeList profileList = atype.getElementsByTagName("profile");
        Element profileName = (Element) profileList.item(0);
        String result = profileName.getAttribute("value");
        result = decorateProfileName(result);
        return result;
    }

    private static String decorateProfileName(String profileName) {
        String result = "<a href='" + profileName + "'>";
        result = result + profileName.substring(profileName.lastIndexOf("/")+1);
        result = result + "</a>";
        return result;
    }
}
