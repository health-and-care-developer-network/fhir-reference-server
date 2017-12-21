package uk.nhs.fhir.metadata;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import uk.nhs.fhir.error.EventHandler;
import uk.nhs.fhir.makehtml.FhirFileRegistry;
import uk.nhs.fhir.makehtml.LoggingEventHandler;
import uk.nhs.fhir.makehtml.XmlFileFinder;

public class FhirResourceCollector {

	private final XmlFileFinder fileFinder;
	private final EventHandler errorHandler;

	public FhirResourceCollector(Path root) {
		this(root, new LoggingEventHandler());
	}
	
	public FhirResourceCollector(Path root, EventHandler errorHandler) {
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