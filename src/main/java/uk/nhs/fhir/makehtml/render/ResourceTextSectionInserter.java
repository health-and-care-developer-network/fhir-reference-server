package uk.nhs.fhir.makehtml.render;

import org.jdom2.Document;
import org.jdom2.Element;

import uk.nhs.fhir.makehtml.data.wrap.WrappedResource;
import uk.nhs.fhir.makehtml.html.jdom2.Elements;
import uk.nhs.fhir.makehtml.html.jdom2.HTMLUtil;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.FileUtils;

public class ResourceTextSectionInserter {
	
	private final ResourceBuilder resourceBuilder;
	
	public ResourceTextSectionInserter(ResourceBuilder resourceBuilder) {
		this.resourceBuilder = resourceBuilder;
	}

	public <T extends WrappedResource<T>> void augmentResource(T source, String inFile, String outFilePath, String newBaseURL) throws Exception {
		ResourceFormatter<T> defaultViewFormatter = source.getDefaultViewFormatter();
		
		HTMLDocSection defaultViewSection = defaultViewFormatter.makeSectionHTML(source);
		SectionedHTMLDoc defaultView = new SectionedHTMLDoc();
		defaultView.addSection(defaultViewSection);
		
		Element textSection = Elements.withChildren("div", 
			defaultView.createStyleSection(),
			Elements.withChildren("div", defaultView.getBodyElements()));
		    
	    String renderedTextSection = HTMLUtil.docToString(new Document(textSection), true, false);
	    
        String augmentedResource = resourceBuilder.addTextSection(FileLoader.loadFile(inFile), renderedTextSection, newBaseURL);
        FileUtils.writeFile(outFilePath, augmentedResource.getBytes("UTF-8"));
	}
}
