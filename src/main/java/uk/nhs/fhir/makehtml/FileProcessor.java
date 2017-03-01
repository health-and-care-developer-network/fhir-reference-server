package uk.nhs.fhir.makehtml;

import static java.io.File.separatorChar;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.FileWriter;

public class FileProcessor {
    private static final Logger LOG = Logger.getLogger(FileProcessor.class.getName());
	
    private final HTMLMaker structureDefinitionHTMLMaker;
    private final HTMLMaker valueSetHTMLMaker;
    private final HTMLMaker operationDefinitionHTMLMaker;
    private final HTMLMaker implementationGuideHTMLMaker;
    
    public FileProcessor(
    	HTMLMaker structureDefinitionHTMLMaker,
    	HTMLMaker valueSetHTMLMaker,
    	HTMLMaker operationDefinitionHTMLMaker,
    	HTMLMaker implementationGuideHTMLMaker) {
    	this.structureDefinitionHTMLMaker = structureDefinitionHTMLMaker;
    	this.valueSetHTMLMaker = valueSetHTMLMaker;
    	this.operationDefinitionHTMLMaker = operationDefinitionHTMLMaker;
    	this.implementationGuideHTMLMaker = implementationGuideHTMLMaker;
    }
    
	boolean processFile(String outPath, String newBaseURL, File folder, File thisFile) {
		String augmentedResource = null;
		
		if(thisFile.isFile()) {
			int matchCount = 0;
			
		    String inFile = thisFile.getPath();
		    String outFilename = outPath + separatorChar + thisFile.getName();
		    LOG.info("\n\n=========================================\nProcessing file: " + inFile + "\n=========================================");
		    Document thisDoc = ReadFile(inFile);
		    
		    // Here we need to see whether it's a StructureDefinition or a ValueSet...
		    if (containsTag(thisDoc, "StructureDefinition")) {
		    	matchCount++;
		        String result = structureDefinitionHTMLMaker.makeHTML(thisDoc);
		        augmentedResource = ResourceBuilder.addTextSectionToResource(FileLoader.loadFile(inFile), result, newBaseURL);
		    }

		    if (containsTag(thisDoc, "ValueSet")) {
		    	matchCount++;
		    	String result = valueSetHTMLMaker.makeHTML(thisDoc);
		        augmentedResource = ResourceBuilder.addTextSectionToValueSet(FileLoader.loadFile(inFile), result, newBaseURL);
		    }

		    if (containsTag(thisDoc, "OperationDefinition")) {
		    	matchCount++;
		    	String result = operationDefinitionHTMLMaker.makeHTML(thisDoc);
		        augmentedResource = ResourceBuilder.addTextSectionTooperationDefinition(FileLoader.loadFile(inFile), result, newBaseURL);
		    }

		    if (containsTag(thisDoc, "ImplementationGuide")) {
		    	matchCount++;
		    	String result = implementationGuideHTMLMaker.makeHTML(thisDoc);
		        augmentedResource = ResourceBuilder.addTextSectionToImplementationGuide(FileLoader.loadFile(inFile), result, newBaseURL);
		    }
		    
		    if (matchCount == 1) {
		    	try {
		            FileWriter.writeFile(outFilename, augmentedResource.getBytes("UTF-8"));
		        } catch (UnsupportedEncodingException ex) {
		            LOG.severe("UnsupportedEncodingException getting resource into UTF-8");
		        }
		    } else if (matchCount == 0) {
		    	LOG.info("Skipping file " + inFile + " - didn't contain any expected tag");
		    } else if (matchCount > 1) {
		    	LOG.severe("Skipping file " + inFile + " - matched " + matchCount + " expected tags, quitting.");
		    	return false;
		    }
		}
		
		return true;
	}
    
    private static boolean containsTag(Document doc, String tagName) {
    	boolean containsTag = doc.getElementsByTagName(tagName).getLength() > 0;
    	if (containsTag) {
        	LOG.info("It's a " + tagName);
    	}
    	
    	return containsTag;
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
