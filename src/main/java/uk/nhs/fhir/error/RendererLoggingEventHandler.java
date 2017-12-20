package uk.nhs.fhir.error;

import java.util.Optional;

import uk.nhs.fhir.makehtml.LoggingEventHandler;

public class RendererLoggingEventHandler extends LoggingEventHandler implements RendererEventHandler {

	private boolean foundErrors = false;
	private boolean foundWarnings = false;

	@Override
	public void ignore(String info, Optional<Exception> throwable) {
		super.ignore(info, throwable);
	}

	@Override
	public void log(String info, Optional<Exception> throwable) {
		foundWarnings = true;
		super.log(info, throwable);
	}

	@Override
	public void error(Optional<String> info, Optional<Exception> throwable) {
		foundErrors = true;
		super.error(info, throwable);
	}

	@Override
	public void displayOutstandingEvents() {
		// all information already shown - nothing to do
	}

	@Override
	public boolean foundErrors() {
		return foundErrors;
	}

	@Override
	public boolean foundWarnings() {
		return foundWarnings;
	}

}
