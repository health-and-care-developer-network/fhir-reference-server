package uk.nhs.fhir.resourcehandlers.dstu2;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.resource.BaseResource;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.resourcehandlers.AbstractResourceProvider;
import uk.nhs.fhir.util.FhirVersion;

/**
 * This extends the AbstractResourceProvider for DSTU2 to use the old ID data
 * type when searching by ID. DSTU2 Resource Providers should extend this.
 * @author Adam Hatherly
 */
public abstract class AbstractResourceProviderDSTU2 extends AbstractResourceProvider {

	public AbstractResourceProviderDSTU2(FilesystemIF dataSource, ResourceType resourceType, Class<? extends IBaseResource> fhirClass) {
		super(dataSource, resourceType, FhirVersion.DSTU2, fhirClass);
	}
    
    public String getTextSection(IBaseResource resource) {
    	BaseResource baseResource = (BaseResource)resource;
    	
    	return baseResource.getText().getDivAsString();
    }
    
    public IBaseResource removeTextSection(IBaseResource resource) {
    	BaseResource baseResource = (BaseResource)resource;
    	
    	// Clear out the generated text
        NarrativeDt textElement = new NarrativeDt();
        textElement.setStatus(NarrativeStatusEnum.GENERATED);
        textElement.setDiv("");
        baseResource.setText(textElement);
    	return baseResource;
    }

}
