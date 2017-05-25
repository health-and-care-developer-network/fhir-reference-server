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
	private final String typedOutputDirectory;
	private final String filename; // used to generate file name
	
	public FormattedOutputSpec(IBaseResource resource, ResourceFormatter formatter, String outputDirectory, String filename) {
		this.resource = resource;
		this.formatter = formatter;
		this.typedOutputDirectory = outputDirectory;
		this.filename = filename;
	}

	public void formatAndSave(String inputPath) throws ParserConfigurationException, IOException {
		ensureOutputDirectoryExists(inputPath);
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

	public String getOutputDirectory(String inputPath) {
		String inputFileName = inputPath.substring(inputPath.lastIndexOf(File.separatorChar) + 1, inputPath.lastIndexOf(".xml"));
		return typedOutputDirectory + inputFileName + File.separator;
	}
	
	public String getOutputPath(String inputPath) {
		return getOutputDirectory(inputPath) + filename;
	}

	private void ensureOutputDirectoryExists(String inputPath) {
		File directory = new File(getOutputDirectory(inputPath));
		if (!directory.exists()) {
			directory.mkdirs();
		}
	}
	
	public ResourceFormatter getFormatter() {
		return formatter;
	}
}
