package uk.nhs.fhir.makehtml;

import java.io.File;

import uk.nhs.fhir.data.wrap.WrappedResource;

public interface RendererErrorHandler {

	public void recordError(File key, WrappedResource<?> value, Exception error);
	public boolean foundErrors();
	public void displayErrors();

}
