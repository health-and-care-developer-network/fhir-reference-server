package uk.nhs.fhir.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import uk.nhs.fhir.datalayer.collections.ResourceEntity;
import uk.nhs.fhir.datalayer.collections.ResourceEntityWithMultipleVersions;
import uk.nhs.fhir.datalayer.collections.VersionNumber;
import uk.nhs.fhir.enums.ResourceType;

import static uk.nhs.fhir.enums.ResourceType.*;

public class PageTemplateHelper {
	
	private static final Logger LOG = Logger.getLogger(PageTemplateHelper.class.getName());
    
    public PageTemplateHelper() {
    	Velocity.init(PropertyReader.getProperties());
    }
    
    public String wrapContentInTemplate(String resourceType, String resourceName, StringBuffer content, String baseURL) {
    	VelocityContext context = new VelocityContext();
    	
    	Template template = null;
    	try {
    	  template = Velocity.getTemplate("/velocity-templates/index.vm");
    	} catch( Exception e ) {
    		e.printStackTrace();
    	}
    	
    	// Put content into template
    	context.put( "page-content", content.toString() );
    	context.put( "resourceType", resourceType );
    	context.put( "resourceName", resourceName );
    	context.put( "baseURL", baseURL );
    	
    	StringWriter sw = new StringWriter();
    	template.merge( context, sw );
    	return sw.toString();
    }
    
    public void streamTemplatedHTMLresponse(HttpServletResponse theResponse, String resourceType,
    									String resourceName, StringBuffer content, String baseURL) {
    	try {
	    	// Initialise the output
	    	PrintWriter outputStream = null;
	        theResponse.setStatus(200);
	        theResponse.setContentType("text/html");
			outputStream = theResponse.getWriter();
	        // Send the content to the output
	        outputStream.append(wrapContentInTemplate(resourceType, resourceName, content, baseURL));
    	} catch (IOException e) {
    		LOG.severe(e.getMessage());
		}
    }
}
