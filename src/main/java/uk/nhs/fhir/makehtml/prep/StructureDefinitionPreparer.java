package uk.nhs.fhir.makehtml.prep;

import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;

public class StructureDefinitionPreparer implements ResourcePreparer<StructureDefinition> {

	@Override
	public void prepare(StructureDefinition structureDefinitionResource, String newBaseURL) {
		// Here (while we have the resource in as a StructureDefinition) we resolve any invalid (c) character in the Copyright section too!
        String copyRight = structureDefinitionResource.getCopyrightElement().getValue();
        if(copyRight != null) {
            copyRight = copyRight.replace("©", "&copy;");
            copyRight = copyRight.replace("\\u00a9", "&copy;");
            structureDefinitionResource.setCopyright(copyRight);
        }
        
        if (newBaseURL != null) {
        	String resourceName = structureDefinitionResource.getName();
        	if (newBaseURL.endsWith("/")) {
        		newBaseURL = newBaseURL.substring(0, newBaseURL.length()-1);
        	}
        	//structureDefinitionResource.setBase(newBaseURL);
        	structureDefinitionResource.setUrl(newBaseURL+"/StructureDefinition/"+resourceName);
        }
	}

}
