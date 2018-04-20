package uk.nhs.fhir.event;

import java.util.Optional;

public interface EventHandler {
	public abstract void event(RendererEventType errorType, String message);
	public abstract void event(RendererEventType errorType, String message, Optional<Exception> error);
}
