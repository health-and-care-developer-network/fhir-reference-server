package uk.nhs.fhir.server_renderer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.event.RendererEventAccumulator;
import uk.nhs.fhir.event.RendererEvents;
import uk.nhs.fhir.render.RendererContext;

/**
 * Logs to standard out as the events are triggered, but accumulates events too, and displays them in a dialog upon rendering completion
 */
public class DeferredDialogEventAccumulator extends RendererEventAccumulator {
	
	private static final Logger LOG = LoggerFactory.getLogger(RendererEventAccumulator.class);
	
	private final JFrame parentWindow;
	private final Optional<File> logFile;

	public DeferredDialogEventAccumulator(JFrame parentWindow) {
		this(parentWindow, Optional.empty());
	}
	
	public DeferredDialogEventAccumulator(JFrame parentWindow, Optional<File> logFile) {
		this.parentWindow = parentWindow;
		this.logFile = logFile;
	}
	
	private String lastPrintedSource = "";
	
	private void copyToFile(String prefix, String text) {
		if (logFile.isPresent()) {
			try (PrintStream ps = new PrintStream(new FileOutputStream(logFile.get(), true))) {

				File source = RendererContext.forThread().getCurrentSource();
				String sourcePath = source.getAbsolutePath();
				if (source != null 
				  && !sourcePath.equals(lastPrintedSource)) {
					lastPrintedSource = sourcePath;
					ps.println("INFO - Events for " + lastPrintedSource);
				}
				
				ps.println(prefix + " - " + text);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override public void logImpl(String info, Optional<Exception> throwable) {
		// store for display in dialog later
		super.logImpl(info, throwable);
		
		String logText = combineLoggableInfo(Optional.of(info), throwable);
		LOG.info(logText);
		copyToFile("WARN", logText);
	};
	
	@Override public void errorImpl(Optional<String> info, Optional<Exception> error) {
		// store for display in dialog later
		super.errorImpl(info, error);
		
		String logText = combineLoggableInfo(info, error);
		LOG.error(logText);
		copyToFile("ERROR", logText);
	};
	
	@Override
	protected void displaySortedEvents(List<RendererEvents> events) {
		final RendererEventDisplayDialog rendererEvents = new RendererEventDisplayDialog(events, parentWindow);

		SwingUtilities.invokeLater(new ShowRendererEventsAWT(rendererEvents));
	}

	private static class ShowRendererEventsAWT implements Runnable {
		private final RendererEventDisplayDialog rendererEvents;
		
		public ShowRendererEventsAWT(RendererEventDisplayDialog rendererEvents) {
			this.rendererEvents = rendererEvents;
		}
		
		@Override
		public void run() {
			rendererEvents.setVisible(true);
		}
	}
}
