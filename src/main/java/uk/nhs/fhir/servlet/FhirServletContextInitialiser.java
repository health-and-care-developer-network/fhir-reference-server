package uk.nhs.fhir.servlet;

import javax.servlet.ServletContext;

import uk.nhs.fhir.util.FhirServerProperties;

public class FhirServletContextInitialiser {

    private boolean initialised = false;
    
	public void initialise(ServletContext context) {
    	if (initialised) {
    		throw new IllegalStateException("Initialising server properties twice");
    	}
    	
		ConfigPathSupplier configPathSupplier = new EnvironmentPropertyConfigPathSupplier();
		FhirServerProperties sharedProperties = new FhirServerProperties(configPathSupplier.get());
		context.setAttribute(FhirServerProperties.SERVLET_CONTEXT_PROPERTY_PROPERTIES, sharedProperties);
		
		context.setAttribute(RequestErrorHandler.SERVLET_CONTEXT_KEY, new RequestErrorHandler());
	}
}
