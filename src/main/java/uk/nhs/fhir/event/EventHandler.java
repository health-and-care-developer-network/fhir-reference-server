package uk.nhs.fhir.event;

import java.util.Optional;

public interface EventHandler {
	
	public abstract void ignore(String info, Optional<Exception> throwable);
	public abstract void log(String info, Optional<Exception> throwable);
	public abstract void error(Optional<String> info, Optional<Exception> throwable);
	public abstract void event(RendererEventType errorType, String message);
	public abstract void event(RendererEventType errorType, String message, Optional<Exception> error);
}
