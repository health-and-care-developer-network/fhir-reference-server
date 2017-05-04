package uk.nhs.fhir.makehtml.prep;

import ca.uhn.fhir.model.dstu2.resource.ValueSet;

public class ValueSetPreparer  implements ResourcePreparer<ValueSet> {

	@Override
	public void prepare(ValueSet vsResource, String newBaseURL) {
		String copyRight = vsResource.getCopyrightElement().getValue();
        if(copyRight != null) {
            copyRight = copyRight.replace("©", "&copy;");
            copyRight = copyRight.replace("\\u00a9", "&copy;");
            vsResource.setCopyright(copyRight);
        }
        
        if (newBaseURL != null) {
        	String resourceName = vsResource.getName();
        	if (newBaseURL.endsWith("/")) {
        		newBaseURL = newBaseURL.substring(0, newBaseURL.length()-1);
        	}
        	//structureDefinitionResource.setBase(newBaseURL);
        	vsResource.setUrl(newBaseURL+"/StructureDefinition/"+resourceName);
        }
	}

}
