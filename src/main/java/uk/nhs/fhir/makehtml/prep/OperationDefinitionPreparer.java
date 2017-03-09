package uk.nhs.fhir.makehtml.prep;

import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;

public class OperationDefinitionPreparer implements ResourcePreparer<OperationDefinition>{

	@Override
	public void prepare(OperationDefinition opDefResource, String newBaseURL) {

        if (newBaseURL != null) {
        	String resourceName = opDefResource.getName();
        	if (newBaseURL.endsWith("/")) {
        		newBaseURL = newBaseURL.substring(0, newBaseURL.length()-1);
        	}
        	//structureDefinitionResource.setBase(newBaseURL);
        	opDefResource.setUrl(newBaseURL+"/StructureDefinition/"+resourceName);
        }
		
	}

}
