package uk.nhs.fhir.makehtml;

import java.util.Optional;

import uk.nhs.fhir.error.EventHandler;

public class NhsFhirContext {
	private static final ThreadLocal<NhsFhirContext> theContext = ThreadLocal.withInitial(NhsFhirContext::new);
	
	public static NhsFhirContext forThread() {
		return theContext.get();
	}
	
	public static void setForThread(NhsFhirContext nhsFhirContext) {
		theContext.set(nhsFhirContext);
	}
	
	private EventHandler eventHandler = null;
	
	public NhsFhirContext() {
		this(new LoggingEventHandler());
	}
	
	public NhsFhirContext(EventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}
	
	public void setEventHandler(EventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}

	// EventHandler passthroughs
	
	public void event(RendererError errorType, String message) {
		event(errorType, message, Optional.empty());
	}
	
	public void event(RendererError errorType, String message, Optional<Exception> error) {
		RendererErrorResponse responseType = RendererEventConfig.getResponse(errorType);
		
		switch (responseType) {
			case IGNORE:
				eventHandler.ignore(message, error);
				break;
			case LOG_WARNING:
				eventHandler.log(message, error);
				break;
			case THROW:
				eventHandler.error(Optional.of(message), error);
				break;
			default:
				throw new IllegalStateException("Unexpected event type: " + responseType.toString());
		}
	}

}
