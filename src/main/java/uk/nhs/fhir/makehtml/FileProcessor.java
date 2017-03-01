package uk.nhs.fhir.makehtml;

import static java.io.File.separatorChar;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.FileWriter;

public class FileProcessor {
    private static final Logger LOG = Logger.getLogger(FileProcessor.class.getName());
	
    private final HTMLMaker structureDefinitionHTMLMaker;
    private final HTMLMaker valueSetHTMLMaker;
    private final HTMLMaker operationDefinitionHTMLMaker;
    private final HTMLMaker implementationGuideHTMLMaker;
    
    private final ResourceBuilder resourceBuilder;
    
    public FileProcessor(
    	HTMLMaker structureDefinitionHTMLMaker,
    	HTMLMaker valueSetHTMLMaker,
    	HTMLMaker operationDefinitionHTMLMaker,
    	HTMLMaker implementationGuideHTMLMaker,
    	ResourceBuilder resourceBuilder) {
    	
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
		    Document thisDoc = ReadFile(inFile);

		    Optional<String> html = buildHTML(inFile, thisDoc);
		    if (html.isPresent()) {
		    	try {
			        augmentedResource = resourceBuilder.addTextSection(FileLoader.loadFile(inFile), html.get(), newBaseURL);
		            FileWriter.writeFile(outFilename, augmentedResource.getBytes("UTF-8"));
		        } catch (UnsupportedEncodingException ex) {
		            LOG.severe("UnsupportedEncodingException getting resource into UTF-8");
		        }
		    }
		}
	}

	Optional<String> buildHTML(String inFile, Document thisDoc) throws Exception {
		String html = null;
		try {
		    String rootTagName = thisDoc.getDocumentElement().getTagName();
			switch (rootTagName) {
		    	case "StructureDefinition":
		    		html = structureDefinitionHTMLMaker.makeHTML(thisDoc);
		    		break;
		    	case "ValueSet":
		    		html = valueSetHTMLMaker.makeHTML(thisDoc);
		    		break;
		    	case "OperationDefinition":
		    		html = operationDefinitionHTMLMaker.makeHTML(thisDoc);
		    		break;
		    	case "ImplementationGuide":
		    		html = implementationGuideHTMLMaker.makeHTML(thisDoc);
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

    /**
     * Routine to read in an XML file to an org.w3c.dom.Document.
     *
     * @param filename
     * @return a Document containing the specified file.
     */
    private Document ReadFile(String filename) {
        Document document = null;
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            document = docBuilder.parse(filename);
            return document;
        } catch (ParserConfigurationException ex) {
            LOG.severe("ParserConfigurationException: " + ex.getMessage());
        } catch (SAXException ex) {
            LOG.severe("SAXException: " + ex.getMessage());
        } catch (IOException ex) {
            LOG.severe("IOException: " + ex.getMessage());
        }
        return document;
    }
}
