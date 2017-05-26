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
	/*private String SDtemplate = null;
    private String VStemplate = null;
    private String ODtemplate = null;
    private String IGtemplate = null;
    private String ServerConformanceTemplate = null;*/
    
    public PageTemplateHelper() {
        /*SDtemplate = FileLoader.loadFileOnClasspath(PropertyReader.getProperty("SDtemplate"));
        VStemplate = FileLoader.loadFileOnClasspath(PropertyReader.getProperty("VStemplate"));
        ODtemplate = FileLoader.loadFileOnClasspath(PropertyReader.getProperty("ODtemplate"));
        IGtemplate = FileLoader.loadFileOnClasspath(PropertyReader.getProperty("IGtemplate"));
        ServerConformanceTemplate = FileLoader.loadFileOnClasspath(PropertyReader.getProperty("ServerConformanceTemplate"));*/
    	Velocity.init(PropertyReader.getProperties());
    }
    
    public String wrapContentInTemplate(ResourceType resourceType, StringBuffer content) {
        /*String outputString = null;
        if (resourceType == null) {
        	outputString = SDtemplate;
        } else {
            if (resourceType == STRUCTUREDEFINITION) {
                outputString = SDtemplate;
            } else if (resourceType == VALUESET) {
                outputString = VStemplate;
            } else if (resourceType == OPERATIONDEFINITION) {
                outputString = ODtemplate;
            } else if (resourceType == IMPLEMENTATIONGUIDE) {
                outputString = IGtemplate;
            } else if (resourceType == CONFORMANCE) {
                outputString = ServerConformanceTemplate;
            }
        }
        outputString = outputString.replaceFirst("\\{\\{PAGE-CONTENT\\}\\}", content.toString());
        return outputString;*/
    	VelocityContext context = new VelocityContext();
    	
    	Template template = null;
    	try {
    	  template = Velocity.getTemplate("/velocity-templates/index.vm");
    	} catch( Exception e ) {
    		e.printStackTrace();
    	}
    	
    	// Put content into template
    	context.put( "page-content", content.toString() );
    	StringWriter sw = new StringWriter();
    	template.merge( context, sw );
    	return sw.toString();
    }
    
    public void streamTemplatedHTMLresponse(HttpServletResponse theResponse, ResourceType resourceType, StringBuffer content) {
    	try {
	    	// Initialise the output
	    	PrintWriter outputStream = null;
	        theResponse.setStatus(200);
	        theResponse.setContentType("text/html");
			outputStream = theResponse.getWriter();
	        // Send the content to the output
	        outputStream.append(wrapContentInTemplate(resourceType, content));
    	} catch (IOException e) {
    		LOG.severe(e.getMessage());
		}
    }
}
