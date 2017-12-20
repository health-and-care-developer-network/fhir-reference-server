package uk.nhs.fhir.error;

public interface RendererEventHandler extends EventHandler {
	
	public boolean foundErrors();
	public boolean foundWarnings();
	
	void displayOutstandingEvents();
}
