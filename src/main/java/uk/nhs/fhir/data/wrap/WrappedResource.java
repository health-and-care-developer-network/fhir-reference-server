package uk.nhs.fhir.data.wrap;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jdom2.Document;
import org.jdom2.Element;

import ca.uhn.fhir.parser.IParser;
import uk.nhs.fhir.data.wrap.dstu2.WrappedDstu2OperationDefinition;
import uk.nhs.fhir.data.wrap.dstu2.WrappedDstu2StructureDefinition;
import uk.nhs.fhir.data.wrap.dstu2.WrappedDstu2ValueSet;
import uk.nhs.fhir.data.wrap.stu3.WrappedStu3CodeSystem;
import uk.nhs.fhir.data.wrap.stu3.WrappedStu3OperationDefinition;
import uk.nhs.fhir.data.wrap.stu3.WrappedStu3StructureDefinition;
import uk.nhs.fhir.data.wrap.stu3.WrappedStu3ValueSet;
import uk.nhs.fhir.makehtml.FormattedOutputSpec;
import uk.nhs.fhir.makehtml.html.jdom2.Elements;
import uk.nhs.fhir.makehtml.html.jdom2.HTMLUtil;
import uk.nhs.fhir.makehtml.prep.ResourcePreparerv2;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;
import uk.nhs.fhir.makehtml.render.SectionedHTMLDoc;
import uk.nhs.fhir.util.FhirContexts;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.FileUtils;
import uk.nhs.fhir.util.StringUtil;

public abstract class WrappedResource<T extends WrappedResource<T>> {

	public abstract IBaseResource getWrappedResource();
	public abstract IBaseMetaType getSourceMeta();
	public abstract FhirVersion getImplicitFhirVersion();
	public abstract String getOutputFolderName();
	public abstract void setUrl(String url);
	
	// Name as used in the resource's URL
	public abstract String getName();
	
	// Update any fields which may need entities escaping
	public abstract void fixHtmlEntities();
	
	public abstract void addHumanReadableText(String textSection);
	
	/**
	 * Returns the formatter which will be used to generate the <Text/> section in the profile when supplied as a raw profile
	 * rather than as a web page for a browser.
	 */
	public abstract ResourceFormatter<T> getDefaultViewFormatter();
	public abstract List<FormattedOutputSpec<T>> getFormatSpecs(String outputDirectory);

	public boolean isDstu2() {
		return getImplicitFhirVersion().equals(FhirVersion.DSTU2);
	};
	public boolean isStu3() {
		return getImplicitFhirVersion().equals(FhirVersion.STU3);
	};
	
	private Optional<IBaseMetaType> getMeta() {
		IBaseMetaType metaInfo = getSourceMeta();
		if (!metaInfo.isEmpty()) {
			return Optional.of(metaInfo);
		} else {
			return Optional.empty();
		}
	}

	public Optional<String> getVersionId() {
		Optional<IBaseMetaType> metaInfo = getMeta();
		if (metaInfo.isPresent()) {
			return Optional.ofNullable(metaInfo.get().getVersionId());
		} else {
			return Optional.empty();
		}
	}
	
	public Optional<String> getLastUpdated() {
		Optional<IBaseMetaType> metaInfo = getMeta();
		if (metaInfo.isPresent()) {
			Date lastUpdated = metaInfo.get().getLastUpdated();
			if (lastUpdated != null) {
				return Optional.of(StringUtil.dateToString(lastUpdated));
			}
		}
		
		return Optional.empty();
	}
	
	public static WrappedResource<?> fromBaseResource(IBaseResource resource) {
		if (resource instanceof ca.uhn.fhir.model.dstu2.resource.StructureDefinition) {
			return new WrappedDstu2StructureDefinition((ca.uhn.fhir.model.dstu2.resource.StructureDefinition)resource);
		} else if (resource instanceof org.hl7.fhir.dstu3.model.StructureDefinition) {
			return new WrappedStu3StructureDefinition((org.hl7.fhir.dstu3.model.StructureDefinition)resource);
		} 
		
		else if (resource instanceof ca.uhn.fhir.model.dstu2.resource.ValueSet) {
			return new WrappedDstu2ValueSet((ca.uhn.fhir.model.dstu2.resource.ValueSet)resource);
		} else if (resource instanceof org.hl7.fhir.dstu3.model.ValueSet) {
			return new WrappedStu3ValueSet((org.hl7.fhir.dstu3.model.ValueSet)resource);
		} 
		
		else if (resource instanceof ca.uhn.fhir.model.dstu2.resource.OperationDefinition) {
			return new WrappedDstu2OperationDefinition((ca.uhn.fhir.model.dstu2.resource.OperationDefinition)resource);
		} else if (resource instanceof org.hl7.fhir.dstu3.model.OperationDefinition) {
			return new WrappedStu3OperationDefinition((org.hl7.fhir.dstu3.model.OperationDefinition)resource);
		}
		
		else if (resource instanceof org.hl7.fhir.dstu3.model.CodeSystem) {
			return new WrappedStu3CodeSystem((org.hl7.fhir.dstu3.model.CodeSystem)resource);
		}
		
		else {
			throw new IllegalStateException("Couldn't make a WrappedResource for " + resource.getClass().getCanonicalName());
		}
	}
	public void saveAugmentedResource(File inFile, WrappedResource<?> parsedResource, String outPath, String newBaseURL) throws Exception {
		// Persist a copy of the xml file with a rendered version embedded in the text section
	    String outputDirectoryName = getOutputFolderName();
	    String outDirPath = outPath + outputDirectoryName; 
		new File(outDirPath).mkdirs();
		
		String outFilePath = outDirPath + File.separatorChar + inFile.getName();
		System.out.println("Generating " + outFilePath);
	    
	    augmentAndWriteResource(parsedResource, outFilePath, newBaseURL);
	}
	
	public void augmentAndWriteResource(WrappedResource<?> parsedResource, String outFilePath, String newBaseURL) throws Exception {
		ResourceFormatter<T> defaultViewFormatter = getDefaultViewFormatter();
		
		HTMLDocSection defaultViewSection = defaultViewFormatter.makeSectionHTML();
		SectionedHTMLDoc defaultView = new SectionedHTMLDoc();
		defaultView.addSection(defaultViewSection);
		
		Element textSection = Elements.withChildren("div", 
			defaultView.createStyleSection(),
			Elements.withChildren("div", defaultView.getBodyElements()));
		    
	    String renderedTextSection = HTMLUtil.docToString(new Document(textSection), true, false);
	    
        String augmentedResource = new ResourcePreparerv2(parsedResource).prepareAndSerialise(renderedTextSection, newBaseURL);
        FileUtils.writeFile(outFilePath, augmentedResource.getBytes("UTF-8"));
	}
	
	public IParser newXmlParser() {
		return FhirContexts.xmlParser(getImplicitFhirVersion());
	}
}
