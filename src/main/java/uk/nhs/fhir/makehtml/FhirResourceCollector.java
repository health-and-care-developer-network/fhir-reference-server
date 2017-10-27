package uk.nhs.fhir.makehtml;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import uk.nhs.fhir.error.FhirErrorHandler;

public class FhirResourceCollector {

	private final XmlFileFinder fileFinder;
	private final FhirErrorHandler errorHandler;

	public FhirResourceCollector(Path root) {
		this(root, new LoggingErrorHandler());
	}
	
	public FhirResourceCollector(Path root, FhirErrorHandler errorHandler) {
		this.fileFinder = new XmlFileFinder(root);
		this.errorHandler = errorHandler;
	}

	public FhirFileRegistry collect() {
		List<File> potentialFhirFiles = fileFinder.findFiles();
    	
    	FhirFileRegistry fhirFileRegistry = new FhirFileRegistry();
    	fhirFileRegistry.registerMany(potentialFhirFiles, errorHandler);
    	
    	return fhirFileRegistry;
	}

}
