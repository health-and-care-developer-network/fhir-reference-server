package uk.nhs.fhir.metadata;

import java.io.File;
import java.nio.file.Path;
import java.util.Map.Entry;

import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.makehtml.FhirResourceCollector;

public class FhirResourceImporter {
	private final Path root;
	private ImportListener[] importListeners;
	
	public FhirResourceImporter(Path root, ImportListener... importListeners) {
		this.root = root;
		this.importListeners = importListeners;
	}
	
	public void doImport() {
		for (Entry<File, WrappedResource<?>> e : new FhirResourceCollector(root).collect()) {
			File sourceFile = e.getKey();
			WrappedResource<?> resource = e.getValue();
			
			for (ImportListener listener : importListeners) {
				listener.doImport(sourceFile, resource);
			}
		}
	}
}
