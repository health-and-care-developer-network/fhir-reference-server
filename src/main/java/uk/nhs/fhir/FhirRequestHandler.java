package uk.nhs.fhir;

import javax.servlet.ServletException;

import ca.uhn.fhir.rest.server.RestfulServer;
import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.util.FHIRVersion;
import uk.nhs.fhir.util.FhirContexts;
import uk.nhs.fhir.util.FhirVersion;

@SuppressWarnings("serial")
public class FhirRequestHandler extends RestfulServer {

	private final FhirVersion fhirVersion;
	private final FilesystemIF dataSource;

	public FhirRequestHandler(FhirVersion fhirVersion, FilesystemIF dataSource) {
		this.fhirVersion = fhirVersion;
		this.dataSource = dataSource;
	}
	
	@Override
    protected void initialize() throws ServletException {
		super.setFhirContext(FhirContexts.forVersion(fhirVersion));
		
	}

}
