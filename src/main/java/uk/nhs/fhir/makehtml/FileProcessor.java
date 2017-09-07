package uk.nhs.fhir.makehtml;

import java.io.File;
import java.util.logging.Logger;

import uk.nhs.fhir.data.wrap.WrappedResource;

public class FileProcessor {
    private static final Logger LOG = Logger.getLogger(FileProcessor.class.getName());
    
    // private final FhirFileParser parser = new FhirFileParser();
    
    private final FhirFileRegistry fhirFileRegistry;
    
    public FileProcessor(FhirFileRegistry fhirFileRegistry) {
    	this.fhirFileRegistry = fhirFileRegistry;
    }
    
	public <T extends WrappedResource<T>> void processFile(String outPath, String newBaseURL, File thisFile, WrappedResource<?> wrappedResource) throws Exception {
		
	    String inFilePath = thisFile.getPath();
	    LOG.info("\n\n=========================================\nProcessing file: " + inFilePath + "\n=========================================");
		
	    wrappedResource.saveAugmentedResource(thisFile, wrappedResource, outPath, newBaseURL, fhirFileRegistry);
		
		for (FormattedOutputSpec<?> formatter : wrappedResource.getFormatSpecs(outPath, fhirFileRegistry)) {
			System.out.println("Generating " + formatter.getOutputPath(inFilePath));
			formatter.formatAndSave(inFilePath, fhirFileRegistry);
		}
	}
}
