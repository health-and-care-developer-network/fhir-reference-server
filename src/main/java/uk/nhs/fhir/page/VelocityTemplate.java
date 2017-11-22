package uk.nhs.fhir.page;

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

	private final String contentTemplateName;
	protected final Optional<String> resourceType;
	private final Optional<String> resourceName;
	
	public VelocityTemplate(String contentTemplateName, Optional<String> resourceType, Optional<String> resourceName) {
		this.contentTemplateName = contentTemplateName;
		this.resourceType = resourceType;
		this.resourceName = resourceName;
	}
	
	public String getHtml() {
		VelocityContext context = sharedShellTemplate.getContext(contentTemplateName, resourceType, resourceName);
		updateContext(context);
		return sharedShellTemplate.merge(context);
	}
}