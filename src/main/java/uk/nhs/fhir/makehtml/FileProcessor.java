package uk.nhs.fhir.makehtml;

import java.nio.file.Path;

import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.makehtml.html.jdom2.Elements;
import uk.nhs.fhir.makehtml.html.jdom2.HTMLUtil;
import uk.nhs.fhir.makehtml.prep.ResourcePreparer;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;
import uk.nhs.fhir.makehtml.render.ResourceFormatterFactory;
import uk.nhs.fhir.makehtml.render.SectionedHTMLDoc;
import uk.nhs.fhir.util.FhirFileUtils;
import uk.nhs.fhir.util.FileLoader;

public class FileProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(FileProcessor.class.getName());
    
    // private final FhirFileParser parser = new FhirFileParser();
    
    private final ResourceFormatterFactory resourceFormatterFactory = new ResourceFormatterFactory(); 
    
	public <T extends WrappedResource<T>> void processFile(RendererFileLocator rendererFileLocator, String newBaseURL) throws Exception {
		
	    WrappedResource<?> resource = RendererFhirContext.forThread().getCurrentParsedResource().get();
		String inFilePath = RendererFhirContext.forThread().getCurrentSource().getPath();
	    
		LOG.info("Processing file: " + inFilePath);
	    
	    saveAugmentedResource(rendererFileLocator, newBaseURL);
		
		for (FormattedOutputSpec<?> formatter : resourceFormatterFactory.allFormatterSpecs(resource, rendererFileLocator)) {
			LOG.debug("Generating " + formatter.getOutputPath(inFilePath).toString());
			formatter.formatAndSave(inFilePath);
		}
	}
	
	public void saveAugmentedResource(RendererFileLocator rendererFileLocator, String newBaseURL) throws Exception {
		WrappedResource<?> resource = RendererFhirContext.forThread().getCurrentParsedResource().get();
		
		// Persist a copy of the xml file with a rendered version embedded in the text section
		Path outDirPath = rendererFileLocator.getRenderingTempOutputDirectory(resource);		
		outDirPath.toFile().mkdirs();
		Path outFilePath = outDirPath.resolve(RendererFhirContext.forThread().getCurrentSource().getName());
		
		LOG.debug("Generating " + outFilePath.toString());	    
		ResourceFormatter<?> defaultViewFormatter = resourceFormatterFactory.defaultFormatter(resource);		
		HTMLDocSection defaultViewSection = defaultViewFormatter.makeSectionHTML();
		
		SectionedHTMLDoc defaultView = new SectionedHTMLDoc();
		defaultView.addSection(defaultViewSection);
		
		Element textSection = Elements.withChildren("div", 
			defaultView.createStyleSection(),
			Elements.withChildren("div", defaultView.getBodyElements()));
		    
	    String renderedTextSection = HTMLUtil.docToString(new Document(textSection), true, false);
	    
        String augmentedResource = new ResourcePreparer(resource).prepareAndSerialise(renderedTextSection, newBaseURL);
        FhirFileUtils.writeFile(outFilePath.toFile(), augmentedResource.getBytes(FileLoader.DEFAULT_ENCODING));
	}
}
