package uk.nhs.fhir.resourcehandlers.stu3;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.resourcehandlers.AbstractResourceProvider;

/**
 * This extends the AbstractResourceProvider for STU3 (onwards?) to use the new ID data
 * type when searching by ID. STU3 Resource Providers should extend this.
 * @author Adam Hatherly
 */
public abstract class AbstractResourceProviderSTU3 extends AbstractResourceProvider {

	public AbstractResourceProviderSTU3(FilesystemIF dataSource) {
		super(dataSource);
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
    public IBaseResource getResourceById(@IdParam IdType theId) {
        return myDatasource.getResourceByID(fhirVersion, theId);
    }

}
