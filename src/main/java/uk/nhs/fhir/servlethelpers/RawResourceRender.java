package uk.nhs.fhir.servlethelpers;

import static uk.nhs.fhir.enums.MimeType.JSON;
import static uk.nhs.fhir.util.ServletUtils.syntaxHighlight;

import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import uk.nhs.fhir.enums.MimeType;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.resourcehandlers.IResourceHelper;
import uk.nhs.fhir.resourcehandlers.ResourceHelperFactory;
import uk.nhs.fhir.resourcehandlers.ResourceWebHandler;
import uk.nhs.fhir.util.FHIRVersion;
import uk.nhs.fhir.util.FhirServerProperties;

public class RawResourceRender {
	
	ResourceWebHandler myWebHandler = null;
	private static String templateDirectory = FhirServerProperties.getProperty("templateDirectory");
	
	public RawResourceRender(ResourceWebHandler webHandler) {
		myWebHandler = webHandler;
	}

    public String renderSingleWrappedRAWResource(RequestDetails theRequestDetails, StringBuffer content,
    									FHIRVersion fhirVersion, ResourceType resourceType, MimeType mimeType) {
    	
    	if (fhirVersion.equals(FHIRVersion.DSTU2)) {
	    	IdDt resourceID = (IdDt)theRequestDetails.getId();
	    	String rawResource = getResourceContent(resourceID, mimeType, fhirVersion, resourceType);
	    	renderSingleWrappedRAWResource(rawResource, content, mimeType);
	    	// Return resource name (for breadcrumb)
	        return myWebHandler.getResourceEntityByID(resourceID).getResourceName();
    	} else if (fhirVersion.equals(FHIRVersion.STU3)) {
	    	IdType resourceID = (IdType)theRequestDetails.getId();
	    	String rawResource = getResourceContent(resourceID, mimeType, fhirVersion, resourceType);
	    	renderSingleWrappedRAWResource(rawResource, content, mimeType);
	    	// Return resource name (for breadcrumb)
	        return myWebHandler.getResourceEntityByID(resourceID).getResourceName();
    	}
    	return null;
    }
    
    public void renderSingleWrappedRAWResource(String rawResource, StringBuffer content, MimeType mimeType) {
    	VelocityContext context = new VelocityContext();
    	Template template = null;
    	try {
    	  template = Velocity.getTemplate(templateDirectory + "raw-resource.vm");
    	} catch( Exception e ) {
    		e.printStackTrace();
    	}
    	
    	context.put( "rawContent", rawResource );
    	
    	if (mimeType == MimeType.JSON) {
        	context.put( "class", "rawJSON" );
        	context.put( "lang", "json" );
    	} else {
    		context.put( "class", "rawXML" );
        	context.put( "lang", "xml" );
    	}
    	
    	StringWriter sw = new StringWriter();
    	template.merge( context, sw );
    	content.append(sw.toString());
    }

    private String getResourceContent(IdDt resourceID, MimeType mimeType, FHIRVersion fhirVersion, ResourceType resourceType) {
    	
    	IResourceHelper helper = ResourceHelperFactory.getResourceHelper(fhirVersion, resourceType);
    	
    	// Clear out the generated text
    	IBaseResource resource = myWebHandler.getResourceByID(resourceID);
        resource = helper.getResourceWithoutTextSection(resource);
        
        if (mimeType == JSON) {
        	return getResourceAsJSON(resource, resourceID, fhirVersion);
        } else {
        	return getResourceAsXML(resource, resourceID, fhirVersion);
        }
    }
    
    private String getResourceContent(IdType resourceID, MimeType mimeType, FHIRVersion fhirVersion, ResourceType resourceType) {
    	
    	IResourceHelper helper = ResourceHelperFactory.getResourceHelper(fhirVersion, resourceType);
    	
    	// Clear out the generated text
    	IBaseResource resource = myWebHandler.getResourceByID(resourceID);
        resource = helper.getResourceWithoutTextSection(resource);
        
        if (mimeType == JSON) {
        	return getResourceAsJSON(resource, resourceID, fhirVersion);
        } else {
        	return getResourceAsXML(resource, resourceID, fhirVersion);
        }
    }
    
    public String getResourceAsXML(IBaseResource resource, IdDt resourceID, FHIRVersion fhirVersion) {
        // Serialise it to XML
        FhirContext ctx = fhirVersion.getContext();
        String serialised = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource);
        // Encode it for HTML output
        serialised = syntaxHighlight(serialised);
        return serialised;
    }
    
    public String getResourceAsXML(IBaseResource resource, IdType resourceID, FHIRVersion fhirVersion) {
        // Serialise it to XML
        FhirContext ctx = fhirVersion.getContext();
        String serialised = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource);
        // Encode it for HTML output
        serialised = syntaxHighlight(serialised);
        return serialised;
    }
    
    public String getResourceAsJSON(IBaseResource resource, IdDt resourceID, FHIRVersion fhirVersion) {
        // Serialise it to JSON
        FhirContext ctx = fhirVersion.getContext();
    	return ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource);
    }
    
    public String getResourceAsJSON(IBaseResource resource, IdType resourceID, FHIRVersion fhirVersion) {
        // Serialise it to JSON
        FhirContext ctx = fhirVersion.getContext();
    	return ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource);
    }
}
