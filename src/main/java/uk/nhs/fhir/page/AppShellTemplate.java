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
	
	public VelocityContext getContext(Optional<String> contentTemplateName, Optional<String> resourceType, Optional<String> resourceName, 
			Optional<String> nonTemplatedContent, String baseURL) {
		VelocityContext context = new VelocityContext();

    	context.put( "contentTemplateName", contentTemplateName.isPresent() ? templateDirectory + contentTemplateName.get() : null );
		context.put( "nonTemplatedContent", nonTemplatedContent.orElse(null) );
    	context.put( "resourceType", resourceType.orElse(null) );
    	context.put( "resourceName", resourceName.orElse(null) );
    	context.put( "baseURL", baseURL );
    	
    	return context;
	}
	
	public String merge(VelocityContext context) {
		StringWriter sw = new StringWriter();
    	template.merge( context, sw );
    	return sw.toString();
	}

}