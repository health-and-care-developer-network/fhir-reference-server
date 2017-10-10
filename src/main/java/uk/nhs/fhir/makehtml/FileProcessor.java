package uk.nhs.fhir.makehtml;

import java.io.File;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;

import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.makehtml.html.jdom2.Elements;
import uk.nhs.fhir.makehtml.html.jdom2.HTMLUtil;
import uk.nhs.fhir.makehtml.prep.ResourcePreparer;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;
import uk.nhs.fhir.makehtml.render.ResourceFormatterFactory;
import uk.nhs.fhir.makehtml.render.SectionedHTMLDoc;
import uk.nhs.fhir.util.FileUtils;

public class FileProcessor {
    private static final Logger LOG = Logger.getLogger(FileProcessor.class.getName());
    
    // private final FhirFileParser parser = new FhirFileParser();
    
    private final FhirFileRegistry fhirFileRegistry;
    private final ResourceFormatterFactory resourceFormatterFactory = new ResourceFormatterFactory(); 
    
    public FileProcessor(FhirFileRegistry fhirFileRegistry) {
    	this.fhirFileRegistry = fhirFileRegistry;
    }
    
	public <T extends WrappedResource<T>> void processFile(String outPath, String newBaseURL, File thisFile, WrappedResource<?> wrappedResource) throws Exception {
		
	    String inFilePath = thisFile.getPath();
	    LOG.info("\n\n=========================================\nProcessing file: " + inFilePath + "\n=========================================");
		
	    saveAugmentedResource(thisFile, wrappedResource, outPath, newBaseURL, fhirFileRegistry);
		
		for (FormattedOutputSpec<?> formatter : resourceFormatterFactory.allFormatterSpecs(wrappedResource, outPath, fhirFileRegistry)) {
			System.out.println("Generating " + formatter.getOutputPath(inFilePath));
			formatter.formatAndSave(inFilePath, fhirFileRegistry);
		}
	}
	
	public void saveAugmentedResource(File inFile, WrappedResource<?> parsedResource, String outPath, String newBaseURL, FhirFileRegistry registry) throws Exception {
		// Persist a copy of the xml file with a rendered version embedded in the text section
	    String outputDirectoryName = parsedResource.getOutputFolderName();
	    String outDirPath = outPath + outputDirectoryName; 
		new File(outDirPath).mkdirs();
		
		String outFilePath = outDirPath + File.separatorChar + inFile.getName();
		System.out.println("Generating " + outFilePath);
	    
	    augmentAndWriteResource(parsedResource, outFilePath, newBaseURL, registry);
	}
	
	public void augmentAndWriteResource(WrappedResource<?> parsedResource, String outFilePath, String newBaseURL, FhirFileRegistry registry) throws Exception {
		ResourceFormatter<?> defaultViewFormatter = resourceFormatterFactory.defaultFormatter(parsedResource, registry);
		
		HTMLDocSection defaultViewSection = defaultViewFormatter.makeSectionHTML();
		SectionedHTMLDoc defaultView = new SectionedHTMLDoc();
		defaultView.addSection(defaultViewSection);
		
		Element textSection = Elements.withChildren("div", 
			defaultView.createStyleSection(),
			Elements.withChildren("div", defaultView.getBodyElements()));
		    
	    String renderedTextSection = HTMLUtil.docToString(new Document(textSection), true, false);
	    
        String augmentedResource = new ResourcePreparer(parsedResource).prepareAndSerialise(renderedTextSection, newBaseURL);
        FileUtils.writeFile(outFilePath, augmentedResource.getBytes("UTF-8"));
	}
}
