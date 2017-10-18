package uk.nhs.fhir.metadata;

import java.nio.file.Path;

import uk.nhs.fhir.datalayer.DefaultFhirFileLocator;
import uk.nhs.fhir.datalayer.FhirFileLocator;

public class FhirResourceImportSupervisor {
	private final FhirResourceImporter importer;
	
	public FhirResourceImportSupervisor(Path fhirImportDestination) {
		this(fhirImportDestination, new DefaultFhirFileLocator());
	}
	
	public FhirResourceImportSupervisor(Path root, FhirFileLocator fhirImportDestination) {
		VersionedFolderImportWriter versionedFolderImportWriter = new VersionedFolderImportWriter(fhirImportDestination);
		this.importer = new FhirResourceImporter(root, versionedFolderImportWriter);
	}

	public void doImport() {
		importer.doImport();
	}
}
