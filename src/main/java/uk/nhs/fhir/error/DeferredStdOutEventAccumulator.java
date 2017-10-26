package uk.nhs.fhir.error;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.util.StringUtil;

public class DeferredStdOutEventAccumulator extends RendererEventAccumulator {
	
	private static final Logger LOG = LoggerFactory.getLogger(RendererEventAccumulator.class);

	@Override
	protected void displaySortedEvents(List<RendererEvents> events) {
		for (RendererEvents fileEvents : events) {
			
			String absolutePath = fileEvents.getFile().getAbsolutePath();
			String type = fileEvents.getResource().getResourceType().getDisplayName();
			String name = fileEvents.getResource().getName();
			Optional<String> url = fileEvents.getResource().getUrl();
			
			String startString = "Error rendering resource:"
			  + "\nFile: " + absolutePath
			  + "\nType: " + type
			  + "\nName: " + name
			  + "\nURL: " + url;
			
			LOG.info(startString);
			
			for (RendererEvent event : fileEvents.getEvents()) {
				EventType eventType = event.getEventType();
				Optional<String> message = event.getMessage();
				Optional<Exception> error = event.getError();
				
				String eventString = eventType.toString() + ": " + message.orElse("");
				if (error.isPresent()) {
					eventString += "\n" + StringUtil.getStackTrace(error.get());
				}
				
				switch (eventType) {
				case ERROR:
					LOG.error(eventString);
					break;
				default:
					LOG.info(eventString);
					break;
				}
			}
			
		}
	}
	
}
