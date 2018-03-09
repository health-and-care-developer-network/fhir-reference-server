package uk.nhs.fhir.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import uk.nhs.fhir.util.FhirServerProperties;

@WebListener
public class SharedServletContext implements ServletContextListener {
	private static ServletContext sharedContext = null; 
	
	public static FhirServerProperties getProperties() {
		return (FhirServerProperties) sharedContext.getAttribute(FhirServerProperties.SERVLET_CONTEXT_PROPERTY_PROPERTIES);
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
		
	}

	public static boolean initialised() {
		return sharedContext != null;
	}
}
