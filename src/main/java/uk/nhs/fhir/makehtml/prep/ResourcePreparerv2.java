package uk.nhs.fhir.makehtml.prep;

import uk.nhs.fhir.data.wrap.WrappedResource;

public class ResourcePreparerv2 {
	private final WrappedResource<?> resource;
	
	public ResourcePreparerv2(WrappedResource<?> resource) {
		this.resource = resource;
	}
	
	public String prepareAndSerialise(String textSection, String newBaseURL) {

		resource.addHumanReadableText(textSection);		
        resource.fixHtmlEntities();
		
		if (newBaseURL != null) {
        	if (newBaseURL.endsWith("/")) {
        		newBaseURL = newBaseURL.substring(0, newBaseURL.length()-1);
        	}
        	
        	resource.setUrl(newBaseURL + "/" + resource.getOutputFolderName() + "/" + resource.getName());
        }
		
		String serialised = resource.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource.getWrappedResource());
        serialised = serialised.replace("Σ", "&#931;");
        return serialised;
	}
}
