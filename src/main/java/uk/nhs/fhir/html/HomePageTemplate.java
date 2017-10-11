package uk.nhs.fhir.html;

import java.util.HashMap;
import java.util.Optional;

import org.apache.velocity.VelocityContext;

public class HomePageTemplate extends VelocityTemplate {
	private final String baseUrl;
	private final HashMap<String, Integer> resourceCounts;

	public HomePageTemplate(String baseUrl, HashMap<String, Integer> resourceCounts) {
		super(Optional.of("home.vm"), Optional.empty(), Optional.empty(), Optional.empty(), baseUrl);
		this.baseUrl = baseUrl;
		this.resourceCounts = resourceCounts;
	}

	@Override
	protected void updateContext(VelocityContext context) {
		context.put( "baseURL", baseUrl );
    	context.put( "counts", resourceCounts );
	}
}
