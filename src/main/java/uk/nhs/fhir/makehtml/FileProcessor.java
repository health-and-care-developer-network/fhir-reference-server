package uk.nhs.fhir.makehtml;

import static java.io.File.separatorChar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.base.Preconditions;

import ca.uhn.fhir.context.FhirContext;
import uk.nhs.fhir.util.SharedFhirContext;

public class FileProcessor {
    private static final Logger LOG = Logger.getLogger(FileProcessor.class.getName());
	
    private final FhirContext fhirContext;
    private final ResourceBuilder resourceBuilder;
    
    public FileProcessor(ResourceBuilder resourceBuilder) {
    	
    	this.fhirContext = SharedFhirContext.get();
    	Preconditions.checkNotNull(resourceBuilder);
    	
    	this.resourceBuilder = resourceBuilder;
    }
    
	public void processFile(String outPath, String newBaseURL, File folder, File thisFile) throws Exception {
		if (thisFile.isFile()) {
			
		    String inFilePath = thisFile.getPath();
		    LOG.info("\n\n=========================================\nProcessing file: " + inFilePath + "\n=========================================");

		    IBaseResource resource = parseFile(thisFile);

		    // Persist a copy of the xml file with a rendered version embedded in the text section
		    String outputDirectoryName = resource.getClass().getSimpleName();
		    String outDirPath = outPath + outputDirectoryName; 
			new File(outDirPath).mkdirs();
			
			String outFilePath = outDirPath + separatorChar + thisFile.getName();
			System.out.println("Generating " + outFilePath);
		    ResourceTextSectionInserter textSectionInserter = new ResourceTextSectionInserter(resourceBuilder);
		    textSectionInserter.augmentResource(resource, inFilePath, outFilePath, newBaseURL);
		    
		    List<FormattedOutputSpec> formatters = ResourceFormatter.formattersForResource(resource, outPath);
		    for (FormattedOutputSpec formatter : formatters) {
		    	try {
					System.out.println("Generating " + formatter.getOutputPath(inFilePath));
			    	formatter.formatAndSave(inFilePath);
		    	} catch (SkipRenderGenerationException e) {
		    		if (NewMain.STRICT) {
		    			throw e;
		    		} else {
		    			e.printStackTrace();
		    		}
		    	}
		    }
		}
	}

	IBaseResource parseFile(File thisFile) throws ParserConfigurationException, IOException, FileNotFoundException {
		try (FileReader fr = new FileReader(thisFile)) {
			IBaseResource resource = fhirContext.newXmlParser().parseResource(fr);
			return resource;
		}
	}
}
