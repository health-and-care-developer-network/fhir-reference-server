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
	protected final String baseURL;
	
	public VelocityTemplate(String contentTemplateName, Optional<String> resourceType, Optional<String> resourceName, String baseURL) {
		this.contentTemplateName = contentTemplateName;
		this.resourceType = resourceType;
		this.resourceName = resourceName;
		this.baseURL = baseURL;
	}
	
	public String getHtml() {
		VelocityContext context = sharedShellTemplate.getContext(contentTemplateName, resourceType, resourceName, baseURL);
		updateContext(context);
		return sharedShellTemplate.merge(context);
	}
}