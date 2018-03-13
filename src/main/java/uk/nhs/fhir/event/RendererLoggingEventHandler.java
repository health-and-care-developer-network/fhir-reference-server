package uk.nhs.fhir.event;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RendererLoggingEventHandler extends AbstractRendererEventHandler {

	private static final Logger LOG = LoggerFactory.getLogger(RendererLoggingEventHandler.class);

	@Override
	public void logImpl(String info, Optional<Exception> throwable) {
		LOG.warn(info);
		if (throwable.isPresent()) {
			throwable.get().printStackTrace();
		}
	}

	@Override
	public void errorImpl(Optional<String> info, Optional<Exception> throwable) {
		if (info.isPresent() && throwable.isPresent()) {
			LOG.error(info.get(), throwable.get());
		} else if (throwable.isPresent()) {
			LOG.error("Error event (no info available)", throwable.get());
		} else if (info.isPresent()) {
			LOG.error(info.get());
		} else {
			LOG.error("Error event without info or throwable");
		}
	}

	@Override
	public void displayOutstandingEvents() {
		// all information already shown - nothing to do
	}
	
	@Override
	public boolean isDeferred() {
		return false;
	}

}
