package uk.nhs.fhir.makehtml;

import ca.uhn.fhir.context.FhirContext;
import com.google.common.base.Preconditions;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jdom2.Document;
import org.jdom2.Element;
import uk.nhs.fhir.makehtml.data.ResourceSectionedHTMLDoc;
import uk.nhs.fhir.util.*;
import uk.nhs.fhir.util.FileWriter;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.List;
import java.util.logging.Logger;

import static java.io.File.separatorChar;

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

		    ResourceSectionedHTMLDoc resourceOutput = parseFile(thisFile);

		    SectionedHTMLDoc output = resourceOutput.getTreeView();
		    
		    boolean writeRenderedHtmlFiles = true;
		    if (writeRenderedHtmlFiles) {
			    Document outputDoc = output.getHTML();
			    String renderedDoc = HTMLUtil.docToString(outputDoc, true, false);
			    
		    	String htmlDirPath = outPath + separatorChar + "html";
		    	File htmlDir = new File(htmlDirPath);
		    	htmlDir.mkdir();
		    	
		    	String htmlOutFilename = htmlDirPath + separatorChar + thisFile.getName().replace(".xml", ".html");
		    	FileWriter.writeFile(htmlOutFilename, renderedDoc.getBytes("UTF-8"));
		    }

	    	try {
	    		Element textSection = Elements.withChildren("div", 
    				output.createStyleSection(),
    				Elements.withChildren("div", output.getBodyElements()));
			    
			    String renderedTextSection = HTMLUtil.docToString(new Document(textSection), true, false);
			    
		        augmentedResource = resourceBuilder.addTextSection(FileLoader.loadFile(inFile), renderedTextSection, newBaseURL);
	            FileWriter.writeFile(outFilename, augmentedResource.getBytes("UTF-8"));
	        } catch (UnsupportedEncodingException ex) {
	            LOG.severe("UnsupportedEncodingException getting resource into UTF-8");
	        }
	        if (resourceOutput.getBindings() != null)
            {
                output = resourceOutput.getBindings();
                Document outputDoc = output.getHTML();
                String renderedDoc = HTMLUtil.docToString(outputDoc, true, false);

                String htmlDirPath = outPath + separatorChar + "html";
                File htmlDir = new File(htmlDirPath);
                htmlDir.mkdir();

                String htmlOutFilename = htmlDirPath + separatorChar + thisFile.getName().replace(".xml", ".bindings.html");
                FileWriter.writeFile(htmlOutFilename, renderedDoc.getBytes("UTF-8"));
            }
		}
	}

	ResourceSectionedHTMLDoc parseFile(File thisFile) throws ParserConfigurationException, IOException, FileNotFoundException {
		try (FileReader fr = new FileReader(thisFile)) {
			IBaseResource resource = fhirContext.newXmlParser().parseResource(fr);
			return processResource(resource);
		}
	}

	private <T extends IBaseResource> ResourceSectionedHTMLDoc processResource(T resource) throws ParserConfigurationException {
		List<ResourceFormatter<T>> formatters = ResourceFormatter.factoryForResource(resource);

		ResourceSectionedHTMLDoc docs = new ResourceSectionedHTMLDoc();
		SectionedHTMLDoc doc = new SectionedHTMLDoc();
		SectionedHTMLDoc bindingDoc = null;
		// KGM do we assemble the documents here or pass the sections back for assembly?
		for (ResourceFormatter<T> formatter : formatters) {
			switch (formatter.resourceSectionType) {
				case TREEVIEW:
					doc.addSection(formatter.makeSectionHTML(resource));
					break;
				case BINDING:
					if (bindingDoc == null) { bindingDoc = new SectionedHTMLDoc(); }
					HTMLDocSection section = formatter.makeSectionHTML(resource);
					if (section != null) { bindingDoc.addSection(section); }
					break;

			}

		}
		docs.setTreeView(doc);
		if (bindingDoc!=null) { docs.setBindings(bindingDoc); }
		return docs;
	}
}
