package uk.nhs.fhir.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.util.FhirServerProperties;

@WebListener
public class SharedServletContext implements ServletContextListener {
	private static ServletContext sharedContext = null; 
	
	public static FhirServerProperties getProperties() {
		return (FhirServerProperties) sharedContext.getAttribute(FhirServerProperties.SERVLET_CONTEXT_PROPERTY_PROPERTIES);
	}
	
	public static RequestErrorHandler getErrorHandler() {
		return (RequestErrorHandler) sharedContext.getAttribute(RequestErrorHandler.SERVLET_CONTEXT_KEY);
	}

	@Override
	public void contextDestroyed(ServletContextEvent destroyedEvent) {}

	@Override
	public void contextInitialized(ServletContextEvent initializedEvent) {
		if (sharedContext == null) {
			ServletContext servletContext = initializedEvent.getServletContext();
			new FhirServletContextInitialiser().initialise(servletContext);
			sharedContext = servletContext;
		} else {
			throw new IllegalStateException("FHIR Server recorded multiple contextInitialised events");
		}
		
		postContextInitialised();
	}

	private void postContextInitialised() {
		cacheResourcesAsync();
	}

	private void cacheResourcesAsync() {
		// load existing resources on startup
		new Thread(
			new Runnable() {
				public void run() {
					FilesystemIF.invalidateCache();
				}
			}, "InitServerCache").start();
	}

	public static boolean initialised() {
		return sharedContext != null;
	}
}
