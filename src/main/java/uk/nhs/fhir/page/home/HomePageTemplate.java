package uk.nhs.fhir.page.home;

import java.util.HashMap;
import java.util.Optional;

import org.apache.velocity.VelocityContext;

import uk.nhs.fhir.page.VelocityTemplate;

public class HomePageTemplate extends VelocityTemplate {
	private final String baseUrl;
	private final HashMap<String, Integer> resourceCounts;

	public HomePageTemplate(String baseUrl, HashMap<String, Integer> resourceCounts) {
		super("home.vm", Optional.empty(), Optional.empty(), baseUrl);
		this.baseUrl = baseUrl;
		this.resourceCounts = resourceCounts;
	}

	@Override
	protected void updateContext(VelocityContext context) {
		context.put( "baseURL", baseUrl );
    	context.put( "counts", resourceCounts );
	}
}