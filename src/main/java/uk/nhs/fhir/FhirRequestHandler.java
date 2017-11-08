package uk.nhs.fhir;

import ca.uhn.fhir.rest.server.RestfulServer;
import uk.nhs.fhir.util.FhirContexts;
import uk.nhs.fhir.util.FhirVersion;

@SuppressWarnings("serial")
public class FhirRequestHandler extends RestfulServer {

	public FhirRequestHandler(FhirVersion fhirVersion) {
		super(FhirContexts.forVersion(fhirVersion));
	}

}
