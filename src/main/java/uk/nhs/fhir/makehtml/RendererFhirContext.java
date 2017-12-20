package uk.nhs.fhir.makehtml;

import java.io.File;
import java.util.Optional;

import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.error.EventHandler;

public class RendererFhirContext extends NhsFhirContext {
	
	public static RendererFhirContext forThread() {
		return (RendererFhirContext)NhsFhirContext.forThread();
	}
	
	private FhirFileRegistry fhirFileRegistry = new FhirFileRegistry();
	private File currentSource = null;
	private Optional<WrappedResource<?>> currentParsedResource = null;
	
	public RendererFhirContext(FhirFileRegistry fhirFileRegistry, EventHandler errorDisplayer) {
		super(errorDisplayer);
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
