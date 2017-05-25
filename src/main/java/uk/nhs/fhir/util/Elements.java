package uk.nhs.fhir.util;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Text;

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
    	Content child = new Text(text);
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
	
    public static Element withAttributesAndChildren(String name, List<Attribute> attributes, List<? extends Content> children){
    	Preconditions.checkNotNull(name, "name");
		Preconditions.checkNotNull(attributes, "attributes");
		Preconditions.checkNotNull(children, "children");
        
        Element e = new Element(name, HTML_NS_URL);
        attributes.forEach(e::setAttribute);
        children.forEach(e::addContent);
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
}
