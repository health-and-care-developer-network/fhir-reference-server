package uk.nhs.fhir.page.home;

import java.util.HashMap;
import java.util.Optional;

import org.apache.velocity.VelocityContext;

import uk.nhs.fhir.page.VelocityTemplate;

public class HomePageTemplate extends VelocityTemplate {
	private final HashMap<String, Integer> resourceCounts;

	public HomePageTemplate(String baseUrl, HashMap<String, Integer> resourceCounts) {
		super("home.vm", Optional.empty(), Optional.empty());
		this.resourceCounts = resourceCounts;
	}

	@Override
	protected void updateContext(VelocityContext context) {
    	context.put( "counts", resourceCounts );
	}
}
