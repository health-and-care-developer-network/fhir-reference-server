package uk.nhs.fhir.makehtml;

import static java.io.File.separatorChar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jdom2.Document;

import com.google.common.base.Preconditions;

import ca.uhn.fhir.context.FhirContext;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.FileWriter;
import uk.nhs.fhir.util.HTMLUtil;
import uk.nhs.fhir.util.SectionedHTMLDoc;
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
		String augmentedResource = null;
		
		if (thisFile.isFile()) {
			
		    String inFile = thisFile.getPath();
		    String outFilename = outPath + separatorChar + thisFile.getName();
		    LOG.info("\n\n=========================================\nProcessing file: " + inFile + "\n=========================================");
		    
		    Document output = parseFile(thisFile);

		    String renderedDoc = HTMLUtil.docToString(output, true, false);
		    
		    boolean writeRenderedHtmlFiles = true;
		    if (writeRenderedHtmlFiles) {
		    	String htmlDirPath = outPath + separatorChar + "html";
		    	File htmlDir = new File(htmlDirPath);
		    	htmlDir.mkdir();
		    	String htmlOutFilename = htmlDirPath + separatorChar + thisFile.getName().replace(".xml", ".html");
		    	FileWriter.writeFile(htmlOutFilename, renderedDoc.getBytes("UTF-8"));
		    }

	    	try {
		        augmentedResource = resourceBuilder.addTextSection(FileLoader.loadFile(inFile), renderedDoc, newBaseURL);
	            FileWriter.writeFile(outFilename, augmentedResource.getBytes("UTF-8"));
	        } catch (UnsupportedEncodingException ex) {
	            LOG.severe("UnsupportedEncodingException getting resource into UTF-8");
	        }
		}
	}

	Document parseFile(File thisFile) throws ParserConfigurationException, IOException, FileNotFoundException {
		try (FileReader fr = new FileReader(thisFile)) {
			IBaseResource resource = fhirContext.newXmlParser().parseResource(fr);
			return processResource(resource);
		}
	}

	private <T extends IBaseResource> Document processResource(T resource) throws ParserConfigurationException {
		List<ResourceFormatter<T>> formatters = ResourceFormatter.factoryForResource(resource);
		SectionedHTMLDoc doc = new SectionedHTMLDoc();
		
		for (ResourceFormatter<T> formatter : formatters) {
			doc.addSection(formatter.makeSectionHTML(resource));
		}
		
		return doc.getHTML();
	}
}
