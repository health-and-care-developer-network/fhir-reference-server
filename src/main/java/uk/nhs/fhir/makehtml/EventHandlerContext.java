package uk.nhs.fhir.makehtml;

import uk.nhs.fhir.error.EventHandler;

public class EventHandlerContext {
	private static final ThreadLocal<EventHandler> theEventHandler = ThreadLocal.withInitial(LoggingEventHandler::new);
	
	public static EventHandler forThread() {
		return theEventHandler.get();
	}
	
	public static void setForThread(EventHandler eventHandler) {
		theEventHandler.set(eventHandler);
	}
}
