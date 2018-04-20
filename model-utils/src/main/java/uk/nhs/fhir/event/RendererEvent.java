package uk.nhs.fhir.event;

import java.io.File;
import java.util.Optional;

import uk.nhs.fhir.data.wrap.WrappedResource;

public class RendererEvent {

	private final Optional<String> message;
	private final File sourceFile;
	private final Optional<WrappedResource<?>> resource;
	private final Optional<Exception> error;
	private final EventType eventType;
	
	public static RendererEvent warning(String message, File sourceFile, Optional<WrappedResource<?>> resource) {
		return warning(message, sourceFile, resource, Optional.empty());
	}
	
	public static RendererEvent warning(String message, File sourceFile, Optional<WrappedResource<?>> resource, Optional<Exception> exception) {
		return new RendererEvent(Optional.of(message), sourceFile, resource, exception, EventType.WARNING);
	}
	
	public static RendererEvent error(Optional<String> message, File sourceFile, Optional<WrappedResource<?>> resource, Optional<Exception> error) {
		return new RendererEvent(message, sourceFile, resource, error, EventType.ERROR);
	}
	
	private RendererEvent(Optional<String> message, File sourceFile, Optional<WrappedResource<?>> resource, Optional<Exception> error, EventType eventType) {
		this.message = message;
		this.sourceFile = sourceFile;
		this.resource = resource;
		this.error = error;
		this.eventType = eventType;
	}
	
	public Optional<String> getMessage() {
		return message;
	}

	public File getSourceFile() {
		return sourceFile;
	}
	
	public Optional<WrappedResource<?>> getResource() {
		return resource;
	}
	
	public Optional<Exception> getError() {
		return error;
	}

	public EventType getEventType() {
		return eventType;
	}
}
