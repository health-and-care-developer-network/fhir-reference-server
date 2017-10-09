package uk.nhs.fhir.html;

import java.util.List;
import java.util.Optional;

import org.apache.velocity.VelocityContext;

import uk.nhs.fhir.datalayer.collections.ResourceMetadata;

public class ExtensionsListPageTemplate extends VelocityTemplate {

	private final String baseUrl;
	private final List<ResourceMetadata> extensions;
	
	public ExtensionsListPageTemplate(String baseUrl, List<ResourceMetadata> extensions) {
		super("extensions.vm", Optional.empty(), Optional.of("Extension Registry"), Optional.empty(), baseUrl);
		this.baseUrl = baseUrl;
		this.extensions = extensions;
	}

	@Override
	protected void updateContext(VelocityContext context) {
		context.put( "baseURL", baseUrl );
    	context.put( "extensions", extensions );
	}

}