package uk.nhs.fhir.data.wrap;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import uk.nhs.fhir.data.codesystem.FhirIdentifier;
import uk.nhs.fhir.data.message.MessageDefinitionFocus;
import uk.nhs.fhir.data.message.MessageResponse;

public abstract class WrappedMessageDefinition extends WrappedResource<WrappedMessageDefinition> {

	public abstract Optional<String> getDescription();
	public abstract String getStatus();
	public abstract Optional<String> getTitle();
	public abstract Optional<FhirIdentifier> getIdentifier();
	public abstract Date getDate();
	public abstract Optional<String> getCopyright();
	public abstract String getEvent();
	public abstract Optional<String> getCategory();
	public abstract List<MessageResponse> getAllowedResponses();

	public abstract MessageDefinitionFocus getFocus();
	
	@Override
	public String getCrawlerDescription() {
		return getDescription().orElse(getName());
	}
	
}
