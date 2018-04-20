package uk.nhs.fhir.data.wrap;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import uk.nhs.fhir.data.codesystem.FhirIdentifier;
import uk.nhs.fhir.data.message.MessageDefinitionFocus;
import uk.nhs.fhir.data.message.MessageResponse;

public abstract class WrappedMessageDefinition extends WrappedResource<WrappedMessageDefinition> {

	public static final Set<String> PERMITTED_VERSION_STRINGS = ImmutableSet.copyOf(Sets.newHashSet("any", "latest release"));
	
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
