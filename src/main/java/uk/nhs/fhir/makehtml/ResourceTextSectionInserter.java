package uk.nhs.fhir.makehtml;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jdom2.Document;
import org.jdom2.Element;

import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import uk.nhs.fhir.makehtml.opdef.OperationDefinitionFormatter;
import uk.nhs.fhir.makehtml.structdef.StructureDefinitionSnapshotFormatter;
import uk.nhs.fhir.util.Elements;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.FileWriter;
import uk.nhs.fhir.util.HTMLUtil;
import uk.nhs.fhir.util.SectionedHTMLDoc;

public class ResourceTextSectionInserter {
	
	private final ResourceBuilder resourceBuilder;
	
	public ResourceTextSectionInserter(ResourceBuilder resourceBuilder) {
		this.resourceBuilder = resourceBuilder;
	}

	public void augmentResource(IBaseResource source, String inFile, String outFilePath, String newBaseURL) throws Exception {
		ResourceFormatter defaultViewFormatter = getDefaultViewFormatter(source); 
		
		HTMLDocSection defaultViewSection = defaultViewFormatter.makeSectionHTML(source);
		SectionedHTMLDoc defaultView = new SectionedHTMLDoc();
		defaultView.addSection(defaultViewSection);
		
		Element textSection = Elements.withChildren("div", 
				defaultView.createStyleSection(),
				Elements.withChildren("div", defaultView.getBodyElements()));
		    
	    String renderedTextSection = HTMLUtil.docToString(new Document(textSection), true, false);
	    
        String augmentedResource = resourceBuilder.addTextSection(FileLoader.loadFile(inFile), renderedTextSection, newBaseURL);
        FileWriter.writeFile(outFilePath, augmentedResource.getBytes("UTF-8"));
	}

	private ResourceFormatter getDefaultViewFormatter(IBaseResource source) {
		if (source instanceof StructureDefinition) {
			return new StructureDefinitionSnapshotFormatter();
		} else if (source instanceof OperationDefinition) {
			return new OperationDefinitionFormatter();
		} else if (source instanceof ValueSet) {
			return new ValueSetFormatter();
		} else {
			throw new IllegalArgumentException(source.getClass().getSimpleName());
		}
	}

}
