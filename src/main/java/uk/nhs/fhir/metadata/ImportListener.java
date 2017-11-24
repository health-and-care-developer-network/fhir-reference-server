package uk.nhs.fhir.metadata;

import java.io.File;

import uk.nhs.fhir.data.wrap.WrappedResource;

public interface ImportListener {
	public void doImport(File sourceFile, WrappedResource<?> resource);
}
