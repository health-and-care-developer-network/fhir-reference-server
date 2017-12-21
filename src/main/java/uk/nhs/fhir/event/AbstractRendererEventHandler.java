package uk.nhs.fhir.event;

import java.util.Optional;

import uk.nhs.fhir.error.AbstractEventHandler;

public abstract class AbstractRendererEventHandler extends AbstractEventHandler {
	
	public abstract void displayOutstandingEvents();
	protected abstract void logImpl(String info, Optional<Exception> throwable);
	protected abstract void errorImpl(Optional<String> info, Optional<Exception> error);

	private boolean foundWarnings = false;
	private boolean foundErrors = false;
	
	public boolean foundErrors() {
		return foundErrors;
	}

	public boolean foundWarnings() {
		return foundWarnings;
	}

	public final void log(String info, Optional<Exception> throwable) {
		foundWarnings = true;
		
		logImpl(info, throwable);
	}

	/*
	 * Default implementation
	 */
	@Override
	public void ignore(String info, Optional<Exception> throwable) {
		// nothing
	}
	
	/*
	 * Ensure that we only LoggedRenderingExceptions. This allows us to distinguish between errors that
	 * have been handled and errors that haven't, and prevent errors being reported twice.
	 */
	public final void error(Optional<String> info, Optional<Exception> error) {
		foundErrors = true;
		try {
			errorImpl(info, error);
		} catch (Throwable t) {
			throw new Error("Shouldn't throw exception in error Impl - this will cause errors to be reported twice");
		}
		
		if (info.isPresent()) {
			if (error.isPresent()) {
				throw new LoggedRenderingException(info.get(), error.get());
			} else {
				throw new LoggedRenderingException(info.get());
			}
		} else {
			if (error.isPresent()) {
				throw new LoggedRenderingException(error.get());
			} else {
				throw new LoggedRenderingException();
			}
		}
	}
}
