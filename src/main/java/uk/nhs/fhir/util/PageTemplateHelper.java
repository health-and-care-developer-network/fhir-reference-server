package uk.nhs.fhir.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

public class PageTemplateHelper {
	
	private static final Logger LOG = Logger.getLogger(PageTemplateHelper.class.getName());
	private static String templateDirectory = FhirServerProperties.getProperty("templateDirectory");
    
    public PageTemplateHelper() {
    	Velocity.init(FhirServerProperties.getProperties());
    }
    
    public String wrapContentInTemplate(String resourceType, String resourceName, StringBuilder content, String baseURL) {
    	VelocityContext context = new VelocityContext();
    	
    	Template template = null;
    	try {
    	  template = Velocity.getTemplate(templateDirectory + "app-shell.vm");
    	} catch( Exception e ) {
    		e.printStackTrace();
    	}
    	
    	// Put content into template
    	context.put( "nonTemplatedContent", content.toString() );
    	context.put( "resourceType", resourceType );
    	context.put( "resourceName", resourceName );
    	context.put( "baseURL", baseURL );
    	
    	StringWriter sw = new StringWriter();
    	template.merge( context, sw );
    	return sw.toString();
    }
    
    public void setResponseTextualSuccess(HttpServletResponse theResponse, String wrappedContent) {
    	try {
	    	// Initialise the output
	        theResponse.setStatus(200);
	        theResponse.setContentType("text/html");
	        
	        // Send the content to the output
			theResponse.getWriter().append(wrappedContent);
    	} catch (IOException e) {
    		LOG.severe(e.getMessage());
		}
    }
}
