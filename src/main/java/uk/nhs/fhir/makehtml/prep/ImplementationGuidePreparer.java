package uk.nhs.fhir.makehtml.prep;

import ca.uhn.fhir.model.dstu2.resource.ImplementationGuide;

public class ImplementationGuidePreparer implements ResourcePreparer<ImplementationGuide> {

	@Override
	public void prepare(ImplementationGuide impGuideResource, String newBaseURL) {
        if (newBaseURL != null) {
        	String resourceName = impGuideResource.getName();
        	if (newBaseURL.endsWith("/")) {
        		newBaseURL = newBaseURL.substring(0, newBaseURL.length()-1);
        	}
        	impGuideResource.setUrl(newBaseURL+"/ImplementationGuide/"+resourceName);
        }
	}

}
