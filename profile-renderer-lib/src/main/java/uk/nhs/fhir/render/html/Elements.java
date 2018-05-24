package uk.nhs.fhir.render.html;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.IllegalDataException;
import org.jdom2.Text;
import org.jdom2.Verifier;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class Elements {

	public static final String HTML_NS_URL = "http://www.w3.org/1999/xhtml";
	
	public static Element withText(String name, String text) {
		return withAttributesAndText(name, Lists.newArrayList(), text);
	}
    
    public static Element withAttributeAndText(String name, Attribute attribute, String textContent){
    	return withAttributesAndText(name, Lists.newArrayList(attribute), textContent);
    }
    
    public static Element withAttributesAndText(String name, List<Attribute> attributes, String text) {
    	Content child = text(text);
        return withAttributesAndChild(name, attributes, child);
    }
	
    public static Element newElement(String name) {
    	return withAttributesAndChildren(name, Lists.newArrayList(), Lists.newArrayList());
    }
    
    public static Element withChild(String name, Content child) {
    	return withChildren(name, Lists.newArrayList(child));
    }
    
    public static Element withChildren(String name, Content... children) {
    	return withChildren(name, Arrays.asList(children));
    }

	public static Element withChildren(String name, List<? extends Content> children) {
        return withAttributesAndChildren(name, Lists.newArrayList(), children);
    }

	public static Element withAttribute(String name, Attribute attribute) {
		return withAttributes(name, Lists.newArrayList(attribute));
	}
	
	public static Element withAttributes(String name, List<Attribute> attributes) {
		return withAttributesAndChildren(name, attributes, Lists.<Content>newArrayList());
	}

	public static Element withAttributeAndChild(String name, Attribute attribute, Content child){
    	return withAttributesAndChildren(name, Lists.newArrayList(attribute), Lists.newArrayList(child));
    }

	public static Element withAttributeAndChildren(String name, Attribute attribute, List<? extends Content> children){
    	return withAttributesAndChildren(name, Lists.newArrayList(attribute), children);
    }
	
	public static Element withAttributesAndChild(String name, List<Attribute> attributes, Content child){
    	return withAttributesAndChildren(name, attributes, Lists.newArrayList(child));
    }
	
    public static Element withAttributesAndChildren(String name, List<Attribute> attributes, List<? extends Content> children) {
        Element e = new Element(Preconditions.checkNotNull(name, "name"), HTML_NS_URL);
        Preconditions.checkNotNull(attributes, "attributes").forEach(e::setAttribute);
        Preconditions.checkNotNull(children, "children").forEach(e::addContent);
        return e;
    }
    
    public static Element addClasses(Element element, Set<String> classes) {
    	
    	Attribute attribute = element.getAttribute("class");
		if (attribute != null) {
			String existingClasses = attribute.getValue();
			List<String> existingClassesList = Arrays.asList(existingClasses.split(" "));
			classes.addAll(existingClassesList);
		}
    	
    	if (!classes.isEmpty()) {
    		element.setAttribute("class", String.join(" ", classes));
    	}
    	
    	return element;
    }
    
    public static Text text(String content) {
    	try {
    		return new Text(content);
    	} catch (IllegalDataException e) {
    		
    		StringBuilder newContent = new StringBuilder();
    		
    		for (int i=0; i<content.length(); i++) {
    			Character c = content.charAt(i);
    			if (Verifier.isXMLCharacter((int)c)) {
    				newContent.append(c);
    			} else if (isValidSurrogatePair(content, i)) {
    				i++;
    				newContent.append(c).append(content.charAt(i));
    			} else if (Verifier.isHighSurrogate(c)) {
    				// invalid surrogate pair
    				newContent.append(String.format("0x%d", (int)c));
    				i++;
    				if (i < content.length()) {
    					newContent.append(String.format("0x%d", (int)content.charAt(i)));
    				}
    			} else {
    				newContent.append(String.format("0x%x", (int)c));
    			}
    		}
        	return new Text(newContent.toString());
    	}
    	
    }
    
    private static boolean isValidSurrogatePair(String content, int i) {
		if (i == content.length()-1) {
			return false;
		}
		
		Character c1 = content.charAt(i);
		Character c2 = content.charAt(i+1);
		
		return Verifier.isHighSurrogate(c1)
		  && Verifier.isLowSurrogate(c2)
		  && Verifier.isXMLCharacter(Verifier.decodeSurrogatePair(c1, c2));
	}
}
