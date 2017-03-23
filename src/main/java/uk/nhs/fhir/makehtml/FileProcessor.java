package uk.nhs.fhir.makehtml;

import static java.io.File.separatorChar;

import java.io.File;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jdom2.Document;

import com.google.common.base.Preconditions;

import ca.uhn.fhir.context.FhirContext;
import uk.nhs.fhir.makehtml.old.HTMLMakerOLD;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.FileWriter;
import uk.nhs.fhir.util.HTMLUtil;
import uk.nhs.fhir.util.SectionedHTMLDoc;
import uk.nhs.fhir.util.SharedFhirContext;

public class FileProcessor {
    private static final Logger LOG = Logger.getLogger(FileProcessor.class.getName());
	
    private final FhirContext fhirContext;
    
    private final HTMLMakerOLD structureDefinitionHTMLMaker;
    private final HTMLMakerOLD valueSetHTMLMaker;
    private final HTMLMakerOLD operationDefinitionHTMLMaker;
    private final HTMLMakerOLD implementationGuideHTMLMaker;
    
    private final ResourceBuilder resourceBuilder;
    
    public FileProcessor(
    	HTMLMakerOLD structureDefinitionHTMLMaker,
    	HTMLMakerOLD valueSetHTMLMaker,
    	HTMLMakerOLD operationDefinitionHTMLMaker,
    	HTMLMakerOLD implementationGuideHTMLMaker,
    	ResourceBuilder resourceBuilder) {
    	
    	this.fhirContext = SharedFhirContext.get();
    	
    	Preconditions.checkNotNull(structureDefinitionHTMLMaker);
    	Preconditions.checkNotNull(valueSetHTMLMaker);
    	Preconditions.checkNotNull(operationDefinitionHTMLMaker);
    	Preconditions.checkNotNull(implementationGuideHTMLMaker);
    	Preconditions.checkNotNull(resourceBuilder);
    	
    	this.structureDefinitionHTMLMaker = structureDefinitionHTMLMaker;
    	this.valueSetHTMLMaker = valueSetHTMLMaker;
    	this.operationDefinitionHTMLMaker = operationDefinitionHTMLMaker;
    	this.implementationGuideHTMLMaker = implementationGuideHTMLMaker;
    	this.resourceBuilder = resourceBuilder;
    }
    
	void processFile(String outPath, String newBaseURL, File folder, File thisFile) throws Exception {
		String augmentedResource = null;
		
		if(thisFile.isFile()) {
			
		    String inFile = thisFile.getPath();
		    String outFilename = outPath + separatorChar + thisFile.getName();
		    LOG.info("\n\n=========================================\nProcessing file: " + inFile + "\n=========================================");
		    
		    Document output = null;
		    try (FileReader fr = new FileReader(thisFile)) {
		    	IBaseResource resource = fhirContext.newXmlParser().parseResource(fr);
		    	output = processResource(resource);
		    }

		    String renderedDoc = HTMLUtil.docToString(output, true, false);

	    	try {
		        augmentedResource = resourceBuilder.addTextSection(FileLoader.loadFile(inFile), renderedDoc, newBaseURL);
	            FileWriter.writeFile(outFilename, augmentedResource.getBytes("UTF-8"));
	        } catch (UnsupportedEncodingException ex) {
	            LOG.severe("UnsupportedEncodingException getting resource into UTF-8");
	        }
		}
	}

	private <T extends IBaseResource> Document processResource(T resource) throws ParserConfigurationException {
		ResourceFormatter<T> formatter = ResourceFormatter.factoryForResource(resource);
		SectionedHTMLDoc doc = new SectionedHTMLDoc();
		doc.addSection(formatter.makeSectionHTML(resource));
		return doc.getHTML();
	}

	Optional<String> buildHTML(String inFile, Document document) throws Exception {
		String html = null;
		try {
		    String rootTagName = document.getRootElement().getName();
			switch (rootTagName) {
		    	case "StructureDefinition":
		    		html = structureDefinitionHTMLMaker.makeHTML(document);
		    		break;
		    	case "ValueSet":
		    		html = valueSetHTMLMaker.makeHTML(document);
		    		break;
		    	case "OperationDefinition":
		    		html = operationDefinitionHTMLMaker.makeHTML(document);
		    		break;
		    	case "ImplementationGuide":
		    		html = implementationGuideHTMLMaker.makeHTML(document);
		    		break;
				default:
					LOG.info("Skipping file " + inFile + " - root element tag was " + rootTagName);
					return Optional.empty();
		    }
		} catch (Exception e) {
			throw new Exception("Caught exception processing file " + inFile, e);
		}
		
		return Optional.of(html);
	}
}
