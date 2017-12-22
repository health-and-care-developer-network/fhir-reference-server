package uk.nhs.fhir.event;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingEventHandler extends AbstractEventHandler {

	private static final Logger LOG = LoggerFactory.getLogger(LoggingEventHandler.class);
	
	@Override
	public void ignore(String info, Optional<Exception> throwable) {
		// nothing
	}

	@Override
	public void log(String info, Optional<Exception> throwable) {
		LOG.error(info);
		if (throwable.isPresent()) {
			throwable.get().printStackTrace();
		}
	}

	@Override
	public void error(Optional<String> info, Optional<Exception> throwable) {
		if (throwable.isPresent() && info.isPresent()) {
			throw new IllegalStateException(info.get(), throwable.get());
		} else if (info.isPresent()) {
			throw new IllegalStateException(info.get());
		} else if (throwable.isPresent()) {
			throw new IllegalStateException(throwable.get());
		} else {
			throw new IllegalStateException();
		}
	}

}
