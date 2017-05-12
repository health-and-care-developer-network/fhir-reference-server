package uk.nhs.fhir.makehtml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.instance.model.api.IBaseResource;

import uk.nhs.fhir.util.FileWriter;
import uk.nhs.fhir.util.HTMLUtil;
import uk.nhs.fhir.util.SectionedHTMLDoc;

public class FormattedOutputSpec {
	private final IBaseResource resource;
	private final ResourceFormatter formatter;
	private final String baseOutputDirectory;
	private final String type; // used to generate subdirectory and file extension
	
	public FormattedOutputSpec(IBaseResource resource, ResourceFormatter formatter, String baseOutputDirectory, String type) {
		this.resource = resource;
		this.formatter = formatter;
		this.baseOutputDirectory = baseOutputDirectory;
		this.type = type;
	}

	public void formatAndSave(String inputPath) throws ParserConfigurationException, IOException {
		ensureOutputDirectoryExists();
		String outputPath = getOutputPath(inputPath);
		
		HTMLDocSection sectionHTML = formatter.makeSectionHTML(resource);
		
		if (sectionHTML == null) {
			System.out.println("No section returned by formatter - skipping " + outputPath);
			return;
		}
		
		SectionedHTMLDoc outputDoc = new SectionedHTMLDoc();
		outputDoc.addSection(sectionHTML);
		String outputString = HTMLUtil.docToString(outputDoc.getHTML(), true, false);
		
		if (!FileWriter.writeFile(outputPath, outputString.getBytes("UTF-8"))) {
			throw new IllegalStateException("Failed to write file " + outputPath);
		}
	}
	
	private String getOutputDirectory() {
		return baseOutputDirectory + File.separator + getResourceName() + File.separator + type;
	}

	public String getOutputPath(String inputPath) {
		String inputFileName = inputPath.substring(inputPath.lastIndexOf(File.separatorChar) + 1);
		String outputFileName = inputFileName.replace(".xml", "." + type + ".html");
		return getOutputDirectory() + File.separator + outputFileName;
	}

	private String getResourceName() {
		return resource.getClass().getSimpleName();
	}

	private void ensureOutputDirectoryExists() {
		File directory = new File(getOutputDirectory());
		if (!directory.exists()) {
			directory.mkdirs();
		}
	}
	
	public ResourceFormatter getFormatter() {
		return formatter;
	}
}
