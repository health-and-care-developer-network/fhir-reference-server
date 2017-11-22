package uk.nhs.fhir.page.extensions;

import java.util.List;
import java.util.Optional;

import org.apache.velocity.VelocityContext;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.page.VelocityTemplate;

public class ExtensionsListPageTemplate extends VelocityTemplate {

	private final List<ResourceMetadata> extensions;
	
	public ExtensionsListPageTemplate(String baseUrl, List<ResourceMetadata> extensions) {
		super("extensions.vm", Optional.of("Extension Registry"), Optional.empty());
		this.extensions = extensions;
	}

	@Override
	protected void updateContext(VelocityContext context) {
    	context.put( "extensions", extensions );
	}

}