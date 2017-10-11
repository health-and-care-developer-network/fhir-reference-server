package uk.nhs.fhir.html;

import java.util.Optional;

import org.apache.velocity.VelocityContext;

public class NonVelocityContentTemplate extends VelocityTemplate {

	public NonVelocityContentTemplate(String nonTemplatedContent, Optional<String> resourceType, Optional<String> resourceName, String baseURL) {
		super(Optional.empty(), Optional.of(nonTemplatedContent), resourceType, resourceName, baseURL);
	}

	@Override
	protected void updateContext(VelocityContext context) {
		
	}

}
