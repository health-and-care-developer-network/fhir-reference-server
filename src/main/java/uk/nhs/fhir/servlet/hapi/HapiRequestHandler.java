package uk.nhs.fhir.servlet.hapi;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import ca.uhn.fhir.rest.server.FifoMemoryPagingProvider;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.datalayer.SharedDataSource;
import uk.nhs.fhir.enums.ClientType;
import uk.nhs.fhir.interceptor.ConformanceInterceptor;
import uk.nhs.fhir.resourcehandlers.ResourceProvider;
import uk.nhs.fhir.servlet.IndexServlet;
import uk.nhs.fhir.servlet.SharedServletContext;
import uk.nhs.fhir.servlet.browser.FhirBrowserRequestServlet;
import uk.nhs.fhir.util.FhirContexts;
import uk.nhs.fhir.util.FhirVersion;

@SuppressWarnings("serial")
public abstract class HapiRequestHandler extends RestfulServer {
	private static Logger LOG = LoggerFactory.getLogger(HapiRequestHandler.class);
	
	protected abstract List<IResourceProvider> getResourceProvidersList();
	
	private final FhirVersion fhirVersion;
	private final FilesystemIF dataSource;
	
	public HapiRequestHandler(FhirVersion fhirVersion) {
		this(fhirVersion, SharedDataSource.get());
	}
	
	public HapiRequestHandler(FhirVersion fhirVersion, FilesystemIF dataSource) {
		super(FhirContexts.forVersion(fhirVersion));
		this.fhirVersion = fhirVersion;
		this.dataSource = dataSource;
	}
	
	private final Set<String> browserRequestsHandledByHapi = ImmutableSet.of("/metadata");
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String requestURI = request.getRequestURI();
		
		if ((requestURI.equals("") || requestURI.equals("/"))
		  && Strings.isNullOrEmpty(request.getQueryString())) {
    		// delegate to index page servlet
			RequestDispatcher rd = getServletContext().getNamedDispatcher(IndexServlet.class.getName());
			try {
				rd.forward(request, response);
			} catch (ServletException | IOException e) {
				e.printStackTrace();
			}
			return;
    	} else if (ClientType.getTypeFromHeaders(request).equals(ClientType.BROWSER)
		  && !browserRequestsHandledByHapi.contains(requestURI)) {
			
			getServletContext().getNamedDispatcher(FhirBrowserRequestServlet.BROWSER_SERVLET_NAME).forward(request, response);;
		} else {
			super.service(request, response);
		}
	}
	
	@Override
    protected void initialize() throws ServletException {
        setResourceProviders();
        addInterceptor();
        addPagingProvider();
    }
	
	private void addInterceptor() {
		registerInterceptor(new ConformanceInterceptor(fhirVersion));
	}

	private void addPagingProvider() {
        FifoMemoryPagingProvider pp = new FifoMemoryPagingProvider(10);
        pp.setDefaultPageSize(SharedServletContext.getProperties().getDefaultPageSize());
        pp.setMaximumPageSize(SharedServletContext.getProperties().getMaxPageSize());
        setPagingProvider(pp);
	}

	private void setResourceProviders() {
		List<IResourceProvider> providers = getResourceProvidersList();
		setProviders((Object[])providers.toArray(new IResourceProvider[]{}));
		
		LOG.debug("resourceProviders added");
	}
	
	protected IResourceProvider createResourceProvider(ResourceType resourceType, Class<? extends IBaseResource> clazz) {
		return new ResourceProvider(dataSource, resourceType, fhirVersion, clazz);
	}
}
