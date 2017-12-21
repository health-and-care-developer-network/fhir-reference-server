package uk.nhs.fhir.event;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RendererLoggingEventHandler extends AbstractRendererEventHandler {

	private static final Logger LOG = LoggerFactory.getLogger(RendererLoggingEventHandler.class);

	@Override
	public void logImpl(String info, Optional<Exception> throwable) {
		LOG.error(info);
		if (throwable.isPresent()) {
			throwable.get().printStackTrace();
		}
	}

	@Override
	public void errorImpl(Optional<String> info, Optional<Exception> throwable) {
	}

	@Override
	public void displayOutstandingEvents() {
		// all information already shown - nothing to do
	}

}
