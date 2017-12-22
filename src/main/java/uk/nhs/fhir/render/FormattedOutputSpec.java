package uk.nhs.fhir.render;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.load.FileLoader;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.ResourceFormatter;
import uk.nhs.fhir.render.format.SectionedHTMLDoc;
import uk.nhs.fhir.render.html.jdom2.HTMLUtil;
import uk.nhs.fhir.util.FhirFileUtils;

public class FormattedOutputSpec<T extends WrappedResource<T>> {
	
	private static final Logger LOG = LoggerFactory.getLogger(FormattedOutputSpec.class);
	
	private final ResourceFormatter<T> formatter;
	private final Path typedOutputDirectory;
	private final String filename; // used to generate file name
	
	public FormattedOutputSpec(ResourceFormatter<T> formatter, Path outputDirectory, String filename) {
		this.formatter = formatter;
		this.typedOutputDirectory = outputDirectory;
		this.filename = filename;
	}

	public void formatAndSave(String inputPath) throws ParserConfigurationException, IOException {
		ensureOutputDirectoryExists(inputPath);
		Path outputPath = getOutputPath(inputPath);
		
		HTMLDocSection sectionHTML = formatter.makeSectionHTML();
		
		if (sectionHTML == null) {
			LOG.debug("No section returned by formatter - skipping " + outputPath);
			return;
		}
		
		SectionedHTMLDoc outputDoc = new SectionedHTMLDoc();
		outputDoc.addSection(sectionHTML);
		String outputString = HTMLUtil.docToString(outputDoc.getHTML(), true, false);
		
		if (!FhirFileUtils.writeFile(outputPath.toFile(), outputString.getBytes(FileLoader.DEFAULT_ENCODING))) {
			throw new IllegalStateException("Failed to write file " + outputPath);
		}
	}

	public Path getOutputDirectory(String inputPath) {
		String inputFileName = inputPath.substring(inputPath.lastIndexOf(File.separatorChar) + 1, inputPath.lastIndexOf(".xml"));
		return typedOutputDirectory.resolve(inputFileName);
	}
	
	public Path getOutputPath(String inputPath) {
		return getOutputDirectory(inputPath).resolve(filename);
	}

	private void ensureOutputDirectoryExists(String inputPath) {
		File directory = getOutputDirectory(inputPath).toFile();
		if (!directory.exists()) {
			directory.mkdirs();
		}
	}
	
	public ResourceFormatter<T> getFormatter() {
		return formatter;
	}
}
