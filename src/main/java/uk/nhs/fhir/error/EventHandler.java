package uk.nhs.fhir.error;

import java.util.Optional;

public interface EventHandler {
	
	public void ignore(String info, Optional<Exception> throwable);
	public void log(String info, Optional<Exception> throwable);
	public void error(Optional<String> info, Optional<Exception> throwable);

}
