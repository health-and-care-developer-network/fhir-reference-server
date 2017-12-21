package uk.nhs.fhir.server_renderer;

import java.util.List;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.error.RendererEvents;
import uk.nhs.fhir.event.RendererEventAccumulator;

/**
 * Logs to standard out as the events are triggered, but accumulates events too, and displays them in a dialog upon rendering completion
 */
public class DeferredDialogEventAccumulator extends RendererEventAccumulator {
	
	private static final Logger LOG = LoggerFactory.getLogger(RendererEventAccumulator.class);
	
	private final JFrame parentWindow;

	public DeferredDialogEventAccumulator(JFrame parentWindow) {
		this.parentWindow = parentWindow;
	}
	
	@Override public void logImpl(String info, Optional<Exception> throwable) {
		// store for display in dialog later
		super.logImpl(info, throwable);
		
		LOG.info(combineLoggableInfo(Optional.of(info), throwable));
	};
	
	@Override public void errorImpl(Optional<String> info, Optional<Exception> error) {
		// store for display in dialog later
		super.errorImpl(info, error);
		
		LOG.error(combineLoggableInfo(info, error));
	};
	
	@Override
	protected void displaySortedEvents(List<RendererEvents> events) {
		final RendererEventDisplayDialog rendererEvents = new RendererEventDisplayDialog(events, parentWindow);

		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				rendererEvents.setVisible(true);
			}
		});
	}

}
