package uk.nhs.fhir.render;

import java.io.File;
import java.util.Optional;

import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.util.FhirFileRegistry;

public class RendererContext {
	
	private static final ThreadLocal<RendererContext> theRendererContext = ThreadLocal.withInitial(RendererContext::new);
	
	public static RendererContext forThread() {
		return theRendererContext.get();
	}
	
	private FhirFileRegistry fhirFileRegistry = new FhirFileRegistry();
	private File currentSource = null;
	private Optional<WrappedResource<?>> currentParsedResource = null;
	
	public RendererContext() {
		this(new FhirFileRegistry());
	}
	
	public RendererContext(FhirFileRegistry fhirFileRegistry) {
		this.fhirFileRegistry = fhirFileRegistry;
	}
	
	public FhirFileRegistry getFhirFileRegistry() {
		return fhirFileRegistry;
	}
	
	public void setFhirFileRegistry(FhirFileRegistry fhirFileRegistry) {
		this.fhirFileRegistry = fhirFileRegistry;
	}

	public File getCurrentSource() {
		return currentSource;
	}

	public void setCurrentSource(File newSource) {
		currentSource = newSource;
	}

	public Optional<WrappedResource<?>> getCurrentParsedResource() {
		return currentParsedResource;
	}

	public void setCurrentParsedResource(Optional<WrappedResource<?>> newParsedResource) {
		currentParsedResource = newParsedResource;
	}

	public void clearCurrent() {
		setCurrentParsedResource(Optional.empty());
		setCurrentSource(null);
	}
}
