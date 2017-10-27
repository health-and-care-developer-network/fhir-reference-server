package uk.nhs.fhir.makehtml.render;

import java.io.File;
import java.util.Optional;

import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.error.FhirErrorHandler;
import uk.nhs.fhir.makehtml.FhirFileRegistry;
import uk.nhs.fhir.makehtml.RendererError;
import uk.nhs.fhir.makehtml.RendererErrorConfig;
import uk.nhs.fhir.makehtml.RendererErrorResponse;

public class RendererContext {
	private final FhirFileRegistry knownFiles;
	private final FhirErrorHandler errorDisplayer;
	
	private File currentSource = null;
	private WrappedResource<?> currentParsedResource = null;
	
	public RendererContext(FhirFileRegistry knownFiles, FhirErrorHandler errorDisplayer) {
		this.knownFiles = knownFiles;
		this.errorDisplayer = errorDisplayer;
		
		// only after all fields are initialised, since it leaks a reference to this object
		if (errorDisplayer instanceof RendererErrorHandler) {
			((RendererErrorHandler)errorDisplayer).setContext(this);
		}
	}
	
	public FhirFileRegistry getFhirFileRegistry() {
		return knownFiles;
	}
	
	public FhirErrorHandler getErrorDisplayer() {
		return errorDisplayer;
	}

	public File getCurrentSource() {
		return currentSource;
	}

	public void setCurrentSource(File newSource) {
		currentSource = newSource;
	}

	public WrappedResource<?> getCurrentParsedResource() {
		return currentParsedResource;
	}

	public void setCurrentParsedResource(WrappedResource<?> newParsedResource) {
		currentParsedResource = newParsedResource;
	}

	public void event(RendererError errorType, String message) {
		event(errorType, message, Optional.empty());
	}
	
	
	public void event(RendererError errorType, String message, Optional<Exception> error) {
		RendererErrorResponse responseType = RendererErrorConfig.getResponse(errorType);
		
		switch (responseType) {
			case IGNORE:
				errorDisplayer.ignore(message, error);
				break;
			case LOG_WARNING:
				errorDisplayer.log(message, error);
				break;
			case THROW:
				errorDisplayer.error(Optional.of(message), error);
				break;
			default:
				throw new IllegalStateException("Unexpected event type: " + responseType.toString());
		}
	}
}
