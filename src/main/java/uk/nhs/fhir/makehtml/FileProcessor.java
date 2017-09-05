package uk.nhs.fhir.makehtml;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;

public class FileProcessor {
    private static final Logger LOG = Logger.getLogger(FileProcessor.class.getName());
    
    // private final FhirFileParser parser = new FhirFileParser();
    
    private final FhirFileRegistry fhirFileRegistry;
    
    public FileProcessor(FhirFileRegistry fhirFileRegistry) {
    	this.fhirFileRegistry = fhirFileRegistry;
    }
    
	public <T extends WrappedResource<T>> void processFile(String outPath, String newBaseURL, File folder, File thisFile) throws Exception {
		if (thisFile.isFile()) {
			
		    String inFilePath = thisFile.getPath();
		    LOG.info("\n\n=========================================\nProcessing file: " + inFilePath + "\n=========================================");
			
		    @SuppressWarnings("unchecked")
		    T wrappedResource = (T)fhirFileRegistry.getResource(thisFile);
			
			/*if (wrappedResource instanceof WrappedStructureDefinition
			  && ((WrappedStructureDefinition)wrappedResource).missingSnapshot()) {
				RendererError.handle(RendererError.Key.RESOURCE_WITHOUT_SNAPSHOT, "Resource at " + thisFile.getAbsolutePath() + " doesn't have a snapshot element");
				return;
			}*/
			
			renderAndSave(outPath, newBaseURL, thisFile, inFilePath, wrappedResource);
		}
	}

	<T extends WrappedResource<T>> void renderAndSave(String outPath, String newBaseURL, File thisFile,
			String inFilePath, T wrappedResource) throws Exception, ParserConfigurationException, IOException {
		wrappedResource.saveAugmentedResource(thisFile, wrappedResource, outPath, newBaseURL, fhirFileRegistry);
		
		for (FormattedOutputSpec<T> formatter : wrappedResource.getFormatSpecs(outPath, fhirFileRegistry)) {
			System.out.println("Generating " + formatter.getOutputPath(inFilePath));
			formatter.formatAndSave(inFilePath, fhirFileRegistry);
		}
	}
}
