package uk.nhs.fhir.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import uk.nhs.fhir.enums.ResourceType;

import static uk.nhs.fhir.enums.ResourceType.*;

public class PageTemplateHelper {
	
	private static final Logger LOG = Logger.getLogger(PageTemplateHelper.class.getName());
	private String SDtemplate = null;
    private String VStemplate = null;
    private String ODtemplate = null;
    private String IGtemplate = null;
    private String ServerConformanceTemplate = null;
    
    public PageTemplateHelper() {
        SDtemplate = FileLoader.loadFileOnClasspath("/template/profiles.html");
        VStemplate = FileLoader.loadFileOnClasspath("/template/valuesets.html");
        ODtemplate = FileLoader.loadFileOnClasspath("/template/operations.html");
        IGtemplate = FileLoader.loadFileOnClasspath("/template/guides.html");
        ServerConformanceTemplate = FileLoader.loadFileOnClasspath("/template/serverconformance.html");
    }
    
    public String wrapContentInTemplate(ResourceType resourceType, StringBuffer content) {
        String outputString = null;
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
        return outputString;
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
