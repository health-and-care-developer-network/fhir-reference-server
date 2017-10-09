package uk.nhs.fhir.html;

import java.util.List;
import java.util.Optional;

import org.apache.velocity.VelocityContext;

import uk.nhs.fhir.datalayer.collections.ResourceEntity;

public class ExtensionsListPageTemplate extends VelocityTemplate {

	private final String baseUrl;
	private final List<ResourceEntity> extensions;
	
	public ExtensionsListPageTemplate(String baseUrl, List<ResourceEntity> extensions) {
		super("extensions.vm", Optional.empty(), Optional.empty(), Optional.of("Extension Registry"), baseUrl);
		this.baseUrl = baseUrl;
		this.extensions = extensions;
	}

	@Override
	protected void updateContext(VelocityContext context) {
		context.put( "baseURL", baseUrl );
    	context.put( "extensions", extensions );
	}

}