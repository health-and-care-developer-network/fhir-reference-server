package uk.nhs.fhir.error;

import java.util.Optional;

public interface FhirErrorHandler {
	
	public void ignore(String info, Optional<Exception> throwable);
	public void log(String info, Optional<Exception> throwable);
	public void error(Optional<String> info, Optional<Exception> throwable);

	public boolean foundErrors();
	public boolean foundWarnings();
	
	void displayOutstandingEvents();

}
