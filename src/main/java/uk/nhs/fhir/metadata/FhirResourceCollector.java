package uk.nhs.fhir.metadata;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import uk.nhs.fhir.event.AbstractEventHandler;
import uk.nhs.fhir.event.LoggingEventHandler;
import uk.nhs.fhir.load.RootedXmlFileFinder;
import uk.nhs.fhir.util.FhirFileRegistry;

public class FhirResourceCollector {

	private final RootedXmlFileFinder fileFinder;
	private final AbstractEventHandler errorHandler;

	public FhirResourceCollector(Path root) {
		this(root, new LoggingEventHandler());
	}
	
	public FhirResourceCollector(Path root, AbstractEventHandler errorHandler) {
		this.fileFinder = new RootedXmlFileFinder(root);
		this.errorHandler = errorHandler;
	}

	public FhirFileRegistry collect() {
		List<File> potentialFhirFiles = fileFinder.findFilesRecursively();
    	
    	FhirFileRegistry fhirFileRegistry = new FhirFileRegistry();
    	fhirFileRegistry.registerMany(potentialFhirFiles, errorHandler);
    	
    	return fhirFileRegistry;
	}

}