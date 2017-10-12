package uk.nhs.fhir.html;

import java.util.Optional;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import uk.nhs.fhir.util.FhirServerProperties;

public abstract class VelocityTemplate {
	
	static {
		// As good a place as any to initialise velocity
		Velocity.init(FhirServerProperties.getProperties());
	}

	private static final AppShellTemplate sharedShellTemplate = new AppShellTemplate();
	protected abstract void updateContext(VelocityContext context);

	private final Optional<String> contentTemplateName;
	protected final Optional<String> resourceType;
	private final Optional<String> resourceName;
	private final Optional<String> nonTemplatedContent;
	protected final String baseURL;
	
	public VelocityTemplate(Optional<String> contentTemplateName, Optional<String> nonTemplatedContent, Optional<String> resourceType, Optional<String> resourceName, String baseURL) {
		this.contentTemplateName = contentTemplateName;
		this.nonTemplatedContent = nonTemplatedContent;
		this.resourceType = resourceType;
		this.resourceName = resourceName;
		this.baseURL = baseURL;
	}
	
	public String getHtml() {
		VelocityContext context = sharedShellTemplate.getContext(contentTemplateName, resourceType, resourceName, nonTemplatedContent, baseURL);
		updateContext(context);
		return sharedShellTemplate.merge(context);
	}
}