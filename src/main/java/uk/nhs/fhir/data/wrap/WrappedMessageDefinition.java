package uk.nhs.fhir.data.wrap;

import java.util.Optional;

public abstract class WrappedMessageDefinition extends WrappedResource<WrappedMessageDefinition> {

	public abstract Optional<String> getDescription();
	public abstract String getStatus();
	
	@Override
	public String getCrawlerDescription() {
		return getDescription().orElse(getName());
	}
	
}
