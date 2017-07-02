package uk.nhs.fhir.servlethelpers.dstu2;

import static uk.nhs.fhir.enums.MimeType.JSON;
import static uk.nhs.fhir.enums.ResourceType.IMPLEMENTATIONGUIDE;
import static uk.nhs.fhir.enums.ResourceType.OPERATIONDEFINITION;
import static uk.nhs.fhir.enums.ResourceType.STRUCTUREDEFINITION;
import static uk.nhs.fhir.enums.ResourceType.VALUESET;

import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.resource.ImplementationGuide;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.method.RequestDetails;
import uk.nhs.fhir.enums.FHIRVersion;
import uk.nhs.fhir.enums.MimeType;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.resourcehandlers.IResourceHelper;
import uk.nhs.fhir.resourcehandlers.ResourceHelperFactory;
import uk.nhs.fhir.resourcehandlers.dstu2.ResourceWebHandler;
import uk.nhs.fhir.util.PropertyReader;
import static uk.nhs.fhir.util.ServletUtils.syntaxHighlight;

public class RawResourceRender {
	
	ResourceWebHandler myWebHandler = null;
	private static String templateDirectory = PropertyReader.getProperty("templateDirectory");
	
	public RawResourceRender(ResourceWebHandler webHandler) {
		myWebHandler = webHandler;
	}

    public String renderSingleWrappedRAWResource(RequestDetails theRequestDetails, StringBuffer content,
    									FHIRVersion fhirVersion, ResourceType resourceType, MimeType mimeType) {
    	IdDt resourceID = (IdDt)theRequestDetails.getId();
    	String rawResource = getResourceContent(resourceID, mimeType, fhirVersion, resourceType);
    	renderSingleWrappedRAWResource(rawResource, content, mimeType);
    	// Return resource name (for breadcrumb)
        return myWebHandler.getResourceEntityByID(resourceID).getResourceName();
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
        	return getResourceAsJSON(resource, resourceID);
        } else {
        	return getResourceAsXML(resource, resourceID);
        }
    }
    
    public String getResourceAsXML(IBaseResource resource, IdDt resourceID) {
        // Serialise it to XML
        FhirContext ctx = FhirContext.forDstu2();
        String serialised = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource);
        // Encode it for HTML output
        serialised = syntaxHighlight(serialised);
        return serialised;
        //return serialised.trim().replaceAll("<","&lt;").replaceAll(">","&gt;");
    }
    
    public String getResourceAsJSON(IBaseResource resource, IdDt resourceID) {
        // Serialise it to JSON
        FhirContext ctx = FhirContext.forDstu2();
        return ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource);
    }	
}
