package uk.nhs.fhir;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import uk.nhs.fhir.event.EventHandler;
import uk.nhs.fhir.event.RendererEventType;

public class TestEventHandlerContext implements EventHandler {

	private List<RendererEventType> events = Lists.newArrayList();

	@Override
	public void event(RendererEventType errorType, String message) {
		event(errorType, message, Optional.empty());
	}

	@Override
	public void event(RendererEventType errorType, String message, Optional<Exception> error) {
		events.add(errorType);
	}
	
	public List<RendererEventType> getEvents() {
		return events;
	}
}