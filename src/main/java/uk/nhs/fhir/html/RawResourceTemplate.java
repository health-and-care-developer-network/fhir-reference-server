package uk.nhs.fhir.html;

import java.util.Optional;

import org.apache.velocity.VelocityContext;

import uk.nhs.fhir.enums.MimeType;

public class RawResourceTemplate extends VelocityTemplate {

	private final String rawResource;
	private final MimeType mimeType;
	
	public RawResourceTemplate(Optional<String> nonTemplatedContent,
			Optional<String> resourceType, Optional<String> resourceName, String baseURL, String rawResource, MimeType mimeType) {
		super(Optional.of("raw-resource.vm"), nonTemplatedContent, resourceType, resourceName, baseURL);
		
		this.rawResource = rawResource;
		this.mimeType = mimeType;
	}

	@Override
	protected void updateContext(VelocityContext context) {
		context.put( "rawContent", rawResource );
    	
    	if (mimeType == MimeType.JSON) {
        	context.put( "class", "rawJSON" );
        	context.put( "lang", "json" );
    	} else {
    		context.put( "class", "rawXML" );
        	context.put( "lang", "xml" );
    	}
	}

}
