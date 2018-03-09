package uk.nhs.fhir.page;

import java.io.StringWriter;
import java.util.Optional;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import uk.nhs.fhir.util.FhirServerProperties;

public class AppShellTemplate { 

	private static final String templateDirectory;
	private static final Template template;
	
	static {
		templateDirectory = FhirServerProperties.getProperty("templateDirectory");
		String templateName = "app-shell.vm";
		
    	try {
    		template = Velocity.getTemplate(templateDirectory + templateName);
    	} catch( Exception e ) {
    		throw new VelocityTemplateLoadFailed(templateName, e);
    	}
	}
	
	public VelocityContext getContext(String contentTemplateName, Optional<String> resourceType, Optional<String> resourceName, String crawlerDescription) {
		VelocityContext context = new VelocityContext();

    	context.put( "contentTemplateName", templateDirectory + contentTemplateName );
    	context.put( "resourceType", resourceType.orElse(null) );
    	context.put( "resourceName", resourceName.orElse(null) );
    	context.put( "crawlerDescription", crawlerDescription);
    	
    	return context;
	}
	
	public String merge(VelocityContext context) {
		StringWriter sw = new StringWriter();
    	template.merge( context, sw );
    	return sw.toString();
	}

}