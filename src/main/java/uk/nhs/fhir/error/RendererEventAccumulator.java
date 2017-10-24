package uk.nhs.fhir.error;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.makehtml.render.RendererContext;
import uk.nhs.fhir.makehtml.render.RendererErrorHandler;
import uk.nhs.fhir.util.StringUtil;

public class RendererEventAccumulator implements RendererErrorHandler {

	private static final Logger LOG = LoggerFactory.getLogger(RendererEventAccumulator.class);
	
	private final Map<File, RendererEvents> events = Maps.newHashMap();
	
	private RendererContext context = null;
	
	@Override
	public void setContext(RendererContext context) {
		this.context = context;
	}
	
	public void addEvent(RendererEvent event) {
		File sourceFile = event.getSourceFile();
		
		if (events.containsKey(sourceFile)) {
			events.get(sourceFile).add(event);
		} else {
			events.put(sourceFile, new RendererEvents(event));
		}
	}

	public boolean foundErrors() {
		return events.values()
				.stream()
				.flatMap(events -> events.getEvents().stream())
				.anyMatch(event -> event.getEventType().equals(EventType.ERROR));
	}
	
	public Map<File, RendererEvents> getEvents() {
		return ImmutableMap.copyOf(events);
	}

	@Override
	public void ignore(String info, Optional<Exception> throwable) {
		// nothing
	}

	@Override
	public void log(String info, Optional<Exception> throwable) {
		File source = context.getCurrentSource();
		WrappedResource<?> resource = context.getCurrentParsedResource();
		addEvent(RendererEvent.warning(info, source, resource));
	}

	@Override
	public void error(Optional<String> info, Optional<Exception> error) {
		File source = context.getCurrentSource();
		WrappedResource<?> resource = context.getCurrentParsedResource();
		addEvent(RendererEvent.error(info, source, resource, error));
	}

	private static final Comparator<RendererEvents> BY_TYPE_THEN_NAME =
		new Comparator<RendererEvents>(){
			@Override
			public int compare(RendererEvents o1, RendererEvents o2) {
				WrappedResource<?> resource1 = o1.getResource();
				WrappedResource<?> resource2 = o2.getResource();
				
				String typeName1 = resource1.getResourceType().getDisplayName();
				String typeName2 = resource2.getResourceType().getDisplayName();
				int typeNameComparison = typeName1.compareTo(typeName2);
				if (typeNameComparison != 0) {
					return typeNameComparison;
				}
				
				return o1.getFile().getName().compareTo(o2.getFile().getName());
			}};
	
	@Override
	public void displayOutstandingEvents() {
		List<RendererEvents> events = new ArrayList<>(this.events.values());
		
		// sort first by resourceType, then by filename
		Collections.sort(events, BY_TYPE_THEN_NAME);
		
		for (RendererEvents fileEvents : events) {
			
			String absolutePath = fileEvents.getFile().getAbsolutePath();
			String type = fileEvents.getResource().getResourceType().getDisplayName();
			String name = fileEvents.getResource().getName();
			Optional<String> url = fileEvents.getResource().getUrl();
			
			String startString = "==========================================================="
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
				
				LOG.info(eventString);
			}
			
		}
	}
}
