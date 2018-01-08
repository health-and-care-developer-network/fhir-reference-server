package uk.nhs.fhir.render;

import java.nio.file.Path;

import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.load.FileLoader;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.ResourceFormatter;
import uk.nhs.fhir.render.format.ResourceFormatterFactory;
import uk.nhs.fhir.render.format.SectionedHTMLDoc;
import uk.nhs.fhir.render.html.jdom2.Elements;
import uk.nhs.fhir.render.html.jdom2.HTMLUtil;
import uk.nhs.fhir.util.FhirFileUtils;

public class FileProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(FileProcessor.class.getName());
    
    // private final FhirFileParser parser = new FhirFileParser();
    
    private final ResourceFormatterFactory resourceFormatterFactory = new ResourceFormatterFactory(); 
    
	public <T extends WrappedResource<T>> void processFile(RendererFileLocator rendererFileLocator, String newBaseURL) throws Exception {
		
	    WrappedResource<?> resource = RendererContext.forThread().getCurrentParsedResource().get();
		String inFilePath = RendererContext.forThread().getCurrentSource().getPath();
	    
		LOG.info("Processing file: " + inFilePath);
	    
	    saveAugmentedResource(rendererFileLocator, newBaseURL);
		
		for (FormattedOutputSpec<?> formatter : resourceFormatterFactory.allFormatterSpecs(resource, rendererFileLocator)) {
			LOG.debug("Generating " + formatter.getOutputPath(inFilePath).toString());
			formatter.formatAndSave(inFilePath);
		}
	}
	
	public void saveAugmentedResource(RendererFileLocator rendererFileLocator, String newBaseURL) throws Exception {
		WrappedResource<?> resource = RendererContext.forThread().getCurrentParsedResource().get();
		
		// Persist a copy of the xml file with a rendered version embedded in the text section
		Path outDirPath = rendererFileLocator.getRenderingTempOutputDirectory(resource);		
		if (!outDirPath.toFile().mkdirs()) {
        	throw new IllegalStateException("Failed to create directory [" + outDirPath.toString() + "]");
        }
		Path outFilePath = outDirPath.resolve(RendererContext.forThread().getCurrentSource().getName());
		
		LOG.debug("Generating " + outFilePath.toString());	    
		ResourceFormatter<?> defaultViewFormatter = resourceFormatterFactory.defaultFormatter(resource);		
		HTMLDocSection defaultViewSection = defaultViewFormatter.makeSectionHTML();
		
		SectionedHTMLDoc defaultView = new SectionedHTMLDoc();
		defaultView.addSection(defaultViewSection);
		
		Element textSection = Elements.withChildren("div", 
			defaultView.createStyleSection(),
			Elements.withChildren("div", defaultView.getBodyElements()));
		    
	    String renderedTextSection = HTMLUtil.docToString(new Document(textSection), true, false);
	    
        String augmentedResource = prepareAndSerialise(resource, renderedTextSection, newBaseURL);
        FhirFileUtils.writeFile(outFilePath.toFile(), augmentedResource.getBytes(FileLoader.DEFAULT_ENCODING));
	}
	
	public String prepareAndSerialise(WrappedResource<?> resource, String textSection, String newBaseURL) {

		resource.addHumanReadableText(textSection);		
        resource.fixHtmlEntities();
		
		if (newBaseURL != null) {
        	if (newBaseURL.endsWith("/")) {
        		newBaseURL = newBaseURL.substring(0, newBaseURL.length()-1);
        	}
        	
        	resource.setUrl(newBaseURL + "/" + resource.getOutputFolderName() + "/" + resource.getName());
        }
		
		String serialised = resource.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource.getWrappedResource());
        serialised = serialised.replace("Σ", "&#931;");
        return serialised;
	}
}
