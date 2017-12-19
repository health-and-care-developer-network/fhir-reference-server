package uk.nhs.fhir.util;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.resource.BaseResource;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;

public class Dstu2FhirTextSectionHelper implements FhirTextSectionHelper {
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
