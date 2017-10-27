package uk.nhs.fhir.error;

import java.io.File;
import java.util.List;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.wrap.WrappedResource;

/**
 * The events relating to a single file
 */
public class RendererEvents {

	private final List<RendererEvent> events = Lists.newArrayList();
	private final File file;
	private final WrappedResource<?> resource;
	
	public RendererEvents(File file, WrappedResource<?> resource) {
		this.file = file;
		this.resource = resource;
	}
	
	public RendererEvents(RendererEvent event) {
		this(event.getSourceFile(), event.getResource());
		add(event);
	}

	public File getFile() {
		return file;
	}

	public void add(RendererEvent event) {
		events.add(event);
	}

	public List<RendererEvent> getEvents() {
		return Lists.newArrayList(events);
	}
	
	public WrappedResource<?> getResource() {
		return resource;
	}
}
