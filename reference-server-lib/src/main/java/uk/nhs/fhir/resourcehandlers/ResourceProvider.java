package uk.nhs.fhir.resourcehandlers;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.text.FhirTextSectionHelpers;

public class ResourceProvider implements IResourceProvider {

	private static final Logger LOG = LoggerFactory.getLogger(ResourceProvider.class.getName());

    protected final FilesystemIF dataSource;
    protected final ResourceType resourceType;
    protected final FhirVersion fhirVersion;
    protected final Class<? extends IBaseResource> fhirClass;

    public ResourceProvider(FilesystemIF dataSource, ResourceType resourceType, FhirVersion fhirVersion, Class<? extends IBaseResource> fhirClass) {
    	this.dataSource = dataSource;
        this.resourceType = resourceType;
        this.fhirVersion = fhirVersion;
        this.fhirClass = fhirClass;
        LOG.debug("Created StrutureDefinitionProvider handler to respond to requests for StrutureDefinition resource types.");
    }
    
    /**
     * Get the Type that this IResourceProvider handles, so that the servlet can say it handles that type.
     *
     * @return Class type, used in generating Conformance profile resource.
     */
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return fhirClass;
    }
    
    /**
     * Search by name, so will respond to queries of the form: /StructureDefinition?name:contains=blah
     *
     * @param theNamePart
     * @return
     */
    @Search
	public IBundleProvider searchByName(@RequiredParam(name = "name") StringParam theNamePart) {
    	LOG.debug("Request for resources matching name: " + theNamePart);
    	return new PagedBundleProvider(PagedBundleProvider.SEARCH_BY_NAME, dataSource,
										fhirVersion, resourceType, theNamePart.getValue());
    }
    
    /**
     * Search by URL, so will respond to queries of the form:
     * /StructureDefinition?url=http://acme.org/fhir/StructureDefinition/123
     *
     * @param theURL
     * @return
     */
    @Search
    public IBundleProvider searchByURL(@RequiredParam(name = "url") StringParam theURL) {
    	LOG.debug("Request for resources matching URL: " + theURL);
    	return new PagedBundleProvider(PagedBundleProvider.SEARCH_BY_URL, dataSource,
    									fhirVersion, resourceType, theURL.getValue());
    }

    /**
     * Overall search, will return ALL resources so responds to (for example): /StructureDefinition
     *
     * @return
     */
    @Search
    public IBundleProvider getAllResources() {
        LOG.debug("Request for ALL resources");
        return new PagedBundleProvider(PagedBundleProvider.SEARCH_BY_TYPE, dataSource,
        								fhirVersion, resourceType);
    }

    /**
     * Instance level GET of a resource... This needs to get a Structure Definition resource by name, so will respond to for example:
     *
     * /StructureDefinition/nrls-documentreference-1-0
     *
     * @param theId ID value identifying the resource.
     *
     * @return A StructureDefinition resource
     */
    @Read(version=true)
    public IBaseResource getResourceById(@IdParam IIdType theId) {
        IBaseResource resource = dataSource.getResourceByID(fhirVersion, theId);
        FhirTextSectionHelpers.forVersion(fhirVersion).removeTextSection(resource);
		return resource;
    }

}
