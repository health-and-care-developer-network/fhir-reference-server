package uk.nhs.fhir.servlet;

import javax.servlet.ServletContext;

import uk.nhs.fhir.datalayer.FilesystemIF;
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
		
		triggerCacheResourcesOnInit();
	}

	private void triggerCacheResourcesOnInit() {
		// load existing resources on startup
		new Thread(
			new Runnable() {
				public void run() {
					FilesystemIF.invalidateCache();
				}
			}, "InitServerCache").start();;
	}
}
