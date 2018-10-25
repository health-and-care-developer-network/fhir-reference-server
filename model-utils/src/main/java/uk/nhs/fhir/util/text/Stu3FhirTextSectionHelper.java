package uk.nhs.fhir.util.text;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Narrative.NarrativeStatus;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class Stu3FhirTextSectionHelper implements FhirTextSectionHelper {
    
	@Override
    public String getTextSection(IBaseResource resource) {
    	DomainResource domainResource = (DomainResource)resource;
    	return domainResource.getText().getDivAsString();
    }
    
    @Override
    public IBaseResource removeTextSection(IBaseResource resource) {
    	DomainResource domainResource = (DomainResource)resource;
    	
    	// Clear out the generated text
    	Narrative textElement = new Narrative();
        textElement.setStatusAsString("");
        textElement.setDivAsString("");
        domainResource.setText(textElement);
    	return domainResource;
    }
}
