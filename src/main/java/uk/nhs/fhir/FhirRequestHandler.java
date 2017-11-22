package uk.nhs.fhir;

import java.util.List;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import ca.uhn.fhir.rest.server.FifoMemoryPagingProvider;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.datalayer.SharedDataSource;
import uk.nhs.fhir.servlethelpers.ConformanceInterceptor;
import uk.nhs.fhir.util.FhirContexts;
import uk.nhs.fhir.util.FhirServerProperties;
import uk.nhs.fhir.util.FhirVersion;

@SuppressWarnings("serial")
public class FhirRequestHandler extends RestfulServer {
	private static Logger LOG = LoggerFactory.getLogger(FhirRequestHandler.class);
	
	private final FhirVersion fhirVersion;
	
	public FhirRequestHandler(FhirVersion fhirVersion) {
		super(FhirContexts.forVersion(fhirVersion));
		this.fhirVersion = fhirVersion;
	}

	@Override
    protected void initialize() throws ServletException {
        
        // We create an instance of our persistent layer (either MongoDB or
        // Filesystem), which we'll pass to each resource type handler as we create them
        FilesystemIF dataSource = SharedDataSource.get();
        
        setResourceProviders(dataSource);
        
        addInterceptor();
        
        addPagingProvider();
    }
	
	private void addInterceptor() {
		registerInterceptor(new ConformanceInterceptor(fhirVersion));
	}

	private void addPagingProvider() {
        FifoMemoryPagingProvider pp = new FifoMemoryPagingProvider(10);
        pp.setDefaultPageSize(Integer.parseInt(FhirServerProperties.getProperty("defaultPageSize")));
        pp.setMaximumPageSize(Integer.parseInt(FhirServerProperties.getProperty("maximumPageSize")));
        setPagingProvider(pp);
	}

	private void setResourceProviders(FilesystemIF dataSource) {
		List<IResourceProvider> providers;
		switch (fhirVersion) {
			case DSTU2:
				providers = getDstu2Providers(dataSource);
				break;
			case STU3:
				providers = getStu3Providers(dataSource);
				break;
			default:
				throw new IllegalStateException("No providers available for supported version " + fhirVersion);
		}
		
		setProviders((Object[])providers.toArray(new IResourceProvider[]{}));
		
		LOG.debug("resourceProviders added");
	}

	private List<IResourceProvider> getDstu2Providers(FilesystemIF dataSource) {
		return Lists.newArrayList(
			new uk.nhs.fhir.resourcehandlers.dstu2.StructureDefinitionProvider(dataSource),
        	new uk.nhs.fhir.resourcehandlers.dstu2.ValueSetProvider(dataSource),
        	new uk.nhs.fhir.resourcehandlers.dstu2.OperationDefinitionProvider(dataSource),
        	new uk.nhs.fhir.resourcehandlers.dstu2.ImplementationGuideProvider(dataSource),
        	new uk.nhs.fhir.resourcehandlers.dstu2.ConformanceProvider(dataSource));
	}

	private List<IResourceProvider> getStu3Providers(FilesystemIF dataSource) {
		return Lists.newArrayList(
	        new uk.nhs.fhir.resourcehandlers.stu3.StructureDefinitionProvider(dataSource),
	        new uk.nhs.fhir.resourcehandlers.stu3.ValueSetProvider(dataSource),
	        new uk.nhs.fhir.resourcehandlers.stu3.OperationDefinitionProvider(dataSource),
	        new uk.nhs.fhir.resourcehandlers.stu3.ImplementationGuideProvider(dataSource),
	        new uk.nhs.fhir.resourcehandlers.stu3.CodeSystemProvider(dataSource),
	        new uk.nhs.fhir.resourcehandlers.stu3.ConceptMapProvider(dataSource));
	}
}
