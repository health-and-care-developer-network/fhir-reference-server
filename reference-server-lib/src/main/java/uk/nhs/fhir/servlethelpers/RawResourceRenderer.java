package uk.nhs.fhir.servlethelpers;

import static uk.nhs.fhir.enums.MimeType.JSON;
import static uk.nhs.fhir.util.ServletUtils.syntaxHighlight;

import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.enums.MimeType;
import uk.nhs.fhir.page.raw.RawResourceTemplate;
import uk.nhs.fhir.util.FhirContexts;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.text.FhirTextSectionHelpers;


import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import uk.nhs.fhir.servlet.SharedServletContext;

import org.w3c.dom.Element;

public class RawResourceRenderer {

    public String renderSingleWrappedRAWResourceWithoutText(IBaseResource resource, FhirVersion fhirVersion, String resourceName, ResourceType resourceType, String baseURL, MimeType mimeType) {
        resource = FhirTextSectionHelpers.forVersion(fhirVersion).removeTextSection(resource);
        
        return renderSingleWrappedRAWResource(resource, fhirVersion, Optional.of(resourceName), resourceType, baseURL, mimeType);
    }
    
    public String renderSingleWrappedRAWResource(IBaseResource resource, FhirVersion fhirVersion, Optional<String> resourceName, ResourceType resourceType, String baseURL, MimeType mimeType) {
    	String rawResource = getRawResource(resource, mimeType, fhirVersion);
        return new RawResourceTemplate(Optional.of(resourceType), resourceName, rawResource, mimeType).getHtml("FHIR Server: Raw resource (" + resourceName.orElse(resource.getIdElement().getValueAsString()) + ")");
    }
    
    public String getRawResource(IBaseResource resource, MimeType mimeType, FhirVersion fhirVersion) {
    	FhirContext fhirContext = FhirContexts.forVersion(fhirVersion);
    	if (mimeType == JSON) {
        	return getResourceAsJSON(resource, fhirContext);
        } else {
        	return getResourceAsXML(resource, fhirContext);
        }
	}

	public String getResourceAsXML(IBaseResource resource, FhirContext fhirContext) {
        // Serialise it to XML
        String serialised = fhirContext.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource);
        
        // Encode it for HTML output
        try {
			serialised = syntaxHighlight(updateSoftwareNodes(serialised));
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        
        return serialised;
    }
    
    public String getResourceAsJSON(IBaseResource resource, FhirContext fhirContext) {
        // Serialise it to JSON
    	return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource);
    }
    
    public String updateSoftwareNodes(String xmltag ) throws Exception {
        
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xmltag)));
        StreamResult result = new StreamResult(new StringWriter());
      
        // locate the node(s)
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodes = (NodeList)xpath.evaluate
          ("//software/name", doc, XPathConstants.NODESET);
      
        // make the change
        for (int idx = 0; idx < nodes.getLength(); idx++) {
          ((Element) nodes.item(idx)).setAttribute("value", SharedServletContext.getProperties().getResourceSoftwareName());
      
        }
           
        nodes = (NodeList)xpath.evaluate
              ("//software/version", doc, XPathConstants.NODESET);
          
        // make the change
        for (int idx = 0; idx < nodes.getLength(); idx++) {
              ((Element) nodes.item(idx)).setAttribute("value", SharedServletContext.getProperties().getResourceSoftwareVersion());            
          }
      
              
        // save the result
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform
          (new DOMSource(doc), result);
        return result.getWriter().toString();                       
        }
}
