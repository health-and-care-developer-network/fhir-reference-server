package uk.nhs.fhir.makehtml;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class FhirResourceCollector {

	private final XmlFileFinder fileFinder;

	public FhirResourceCollector(Path root) {
		this.fileFinder = new XmlFileFinder(root);
	}

	public FhirFileRegistry collect() {
		List<File> potentialFhirFiles = fileFinder.findFiles();
    	
    	FhirFileRegistry fhirFileRegistry = new FhirFileRegistry(potentialFhirFiles);
    	
    	return fhirFileRegistry;
	}

}
