package uk.nhs.fhir.resourcehandlers.stu3;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Narrative.NarrativeStatus;
import org.hl7.fhir.instance.model.api.IBaseResource;

import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.resourcehandlers.AbstractResourceProvider;
import uk.nhs.fhir.util.FhirVersion;

/**
 * This extends the AbstractResourceProvider for STU3 (onwards?) to use the new ID data
 * type when searching by ID. STU3 Resource Providers should extend this.
 * @author Adam Hatherly
 */
public abstract class AbstractResourceProviderSTU3 extends AbstractResourceProvider {

	public AbstractResourceProviderSTU3(FilesystemIF dataSource, ResourceType resourceType, Class<? extends IBaseResource> fhirClass) {
		super(dataSource, resourceType, FhirVersion.STU3, fhirClass);
	}
    
    public String getTextSection(IBaseResource resource) {
    	DomainResource domainResource = (DomainResource)resource;
    	return domainResource.getText().getDivAsString();
    }
    
    @Override
    public IBaseResource removeTextSection(IBaseResource resource) {
    	DomainResource domainResource = (DomainResource)resource;
    	
    	// Clear out the generated text
    	Narrative textElement = new Narrative();
        textElement.setStatus(NarrativeStatus.GENERATED);
        textElement.setDivAsString("");
        domainResource.setText(textElement);
    	return domainResource;
    }

}
