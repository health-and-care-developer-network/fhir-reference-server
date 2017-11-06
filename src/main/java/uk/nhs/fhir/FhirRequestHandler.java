package uk.nhs.fhir;

import ca.uhn.fhir.rest.server.RestfulServer;
import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.util.FhirContexts;
import uk.nhs.fhir.util.FhirVersion;

@SuppressWarnings("serial")
public class FhirRequestHandler extends RestfulServer {

	private final FhirVersion fhirVersion;
	private final FilesystemIF dataSource;

	public FhirRequestHandler(FhirVersion fhirVersion, FilesystemIF dataSource) {
		super(FhirContexts.forVersion(fhirVersion));
		
		this.fhirVersion = fhirVersion;
		this.dataSource = dataSource;
	}

}
