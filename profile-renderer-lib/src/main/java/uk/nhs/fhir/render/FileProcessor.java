package uk.nhs.fhir.render;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;
import uk.nhs.fhir.load.FileLoader;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.ResourceFormatter;
import uk.nhs.fhir.render.format.ResourceFormatterFactory;
import uk.nhs.fhir.render.format.SectionedHTMLDoc;
import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.HTMLUtil;
import uk.nhs.fhir.util.FhirFileUtils;
import uk.nhs.fhir.util.text.EscapeUtils;

public class FileProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(FileProcessor.class.getName());
    
    // private final FhirFileParser parser = new FhirFileParser();
    
    private final ResourceFormatterFactory resourceFormatterFactory = new ResourceFormatterFactory(); 
    
	public <T extends WrappedResource<T>> void processFile(RendererFileLocator rendererFileLocator,
														String filename,
														Optional<String> newBaseURL) throws Exception {
		
	    @SuppressWarnings("unchecked")
		T resource = (T)RendererContext.forThread().getCurrentParsedResource().get();
		File currentSource = RendererContext.forThread().getCurrentSource();
		String inFilePath = currentSource.getPath();
	    
		LOG.info("Processing file: " + inFilePath);
	    
		doBOMCheck(currentSource);
		
	    saveAugmentedResource(rendererFileLocator, newBaseURL);
		
		for (FormattedOutputSpec<?> formatter : resourceFormatterFactory.allFormatterSpecs(resource, rendererFileLocator, filename)) {
			LOG.debug("Generating " + formatter.getOutputPath(inFilePath).toString());
			formatter.formatAndSave(inFilePath);
		}
	}
	
    private static final int[] UTF8_BOM = {239, 187, 191};
    
    /**
     * Adam has set up a Git commit hook that everyone should now have which ought to ensure any Byte Order Marks
     * are removed at the point of committing. This should flag up if the system is not working.
     */
    private static void doBOMCheck(File file) {
		if (file.length() < UTF8_BOM.length) {
			return;
		}
		
		int[] head = new int[UTF8_BOM.length];
		try (InputStream input = new FileInputStream(file)) {
			for (int i=0; i<UTF8_BOM.length; i++) {
				head[i] = input.read();
			}
			if (Arrays.equals(head, UTF8_BOM)) {
				EventHandlerContext.forThread().event(RendererEventType.FILE_WITH_BOM, "Found BOM");
			}
		} catch (IOException e) {
			throw new IllegalStateException("Caught exception while checking for BOM", e);
		}
	}
	
	public void saveAugmentedResource(RendererFileLocator rendererFileLocator, Optional<String> newBaseURL) throws Exception {
		WrappedResource<?> resource = RendererContext.forThread().getCurrentParsedResource().get();
		
		// Persist a copy of the xml file with a rendered version embedded in the text section
		Path outDirPath = rendererFileLocator.getRenderingTempOutputDirectory(resource);
		File outDirFile = outDirPath.toFile();
		if (!outDirFile.exists()) {
			if (outDirFile.mkdirs()) {
				LOG.info("Created directory " + outDirFile.getAbsolutePath());
			} else {
				throw new IllegalStateException("Failed to create directory [" + outDirPath.toString() + "]");
			}
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
		
	    String renderedTextSection = HTMLUtil.docToEscapedString(new Document(textSection), true, false);
	    
        String augmentedResource = prepareAndSerialise(resource, renderedTextSection, newBaseURL);
        if (FhirFileUtils.writeFile(outFilePath.toFile(), augmentedResource.getBytes(FileLoader.DEFAULT_ENCODING))) {
        	LOG.debug("Wrote file to " + outFilePath.toAbsolutePath().toString());
        } else {
        	LOG.error("Failed to write file to " + outFilePath.toAbsolutePath().toString());
        }
	}
	
	public String prepareAndSerialise(WrappedResource<?> resource, String textSection, Optional<String> newBaseURL) {
		/*
		textSection = EscapeUtils.escapeTextSection(textSection);
		resource.addHumanReadableText(textSection);
		*/
		resource.clearHumanReadableText();
		
		if (newBaseURL.isPresent()) {
			String replacementUrl = newBaseURL.get();
			
        	if (replacementUrl.endsWith("/")) {
        		replacementUrl = replacementUrl.substring(0, replacementUrl.length()-1);
        	}
        	
        	resource.setUrl(replacementUrl + "/" + resource.getOutputFolderName() + "/" + resource.getName());
        }
		
		String serialised = resource.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource.getWrappedResource());
        return serialised;
	}
}
