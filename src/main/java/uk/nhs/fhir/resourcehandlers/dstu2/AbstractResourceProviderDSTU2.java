package uk.nhs.fhir.resourcehandlers.dstu2;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import uk.nhs.fhir.datalayer.Datasource;
import uk.nhs.fhir.resourcehandlers.AbstractResourceProvider;

/**
 * This extends the AbstractResourceProvider for DSTU2 to use the old ID data
 * type when searching by ID. DSTU2 Resource Providers should extend this.
 * @author Adam Hatherly
 */
public abstract class AbstractResourceProviderDSTU2 extends AbstractResourceProvider {

	public AbstractResourceProviderDSTU2(Datasource dataSource) {
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
    public IBaseResource getResourceById(@IdParam IdDt theId) {
        return myDatasource.getResourceByID(fhirVersion, theId);
    }

}
