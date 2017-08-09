package uk.nhs.fhir.makehtml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.makehtml.html.jdom2.HTMLUtil;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;
import uk.nhs.fhir.makehtml.render.SectionedHTMLDoc;
import uk.nhs.fhir.util.FileUtils;

public class FormattedOutputSpec<T extends WrappedResource<T>> {
	private final T resource;
	private final ResourceFormatter<T> formatter;
	private final String typedOutputDirectory;
	private final String filename; // used to generate file name
	
	public FormattedOutputSpec(T resource, ResourceFormatter<T> formatter, String outputDirectory, String filename) {
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
		
		if (!FileUtils.writeFile(outputPath, outputString.getBytes("UTF-8"))) {
			throw new IllegalStateException("Failed to write file " + outputPath);
		}
	}

	public String getOutputDirectory(String inputPath) {
		String inputFileName = inputPath.substring(inputPath.lastIndexOf(File.separatorChar) + 1, inputPath.lastIndexOf(".xml"));
		return typedOutputDirectory + resource.getOutputFolderName() + File.separator + inputFileName + File.separator;
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
	
	public ResourceFormatter<T> getFormatter() {
		return formatter;
	}
}
