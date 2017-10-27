package uk.nhs.fhir.makehtml.render;

import uk.nhs.fhir.error.FhirErrorHandler;

public interface RendererErrorHandler extends FhirErrorHandler {

	void setContext(RendererContext rendererContext);

}
