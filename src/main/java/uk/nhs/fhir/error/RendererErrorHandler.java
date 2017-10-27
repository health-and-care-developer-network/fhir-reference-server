package uk.nhs.fhir.error;

import uk.nhs.fhir.makehtml.render.RendererContext;

public interface RendererErrorHandler extends FhirErrorHandler {
	public void setContext(RendererContext context);	
}
