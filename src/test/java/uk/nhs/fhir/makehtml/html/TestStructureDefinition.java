package uk.nhs.fhir.makehtml.html;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import uk.nhs.fhir.makehtml.ResourceFormatter;
import uk.nhs.fhir.makehtml.prep.StructureDefinitionPreparer;
import uk.nhs.fhir.util.HTMLUtil;
import uk.nhs.fhir.util.SectionedHTMLDoc;
import uk.nhs.fhir.util.SharedFhirContext;

public class TestStructureDefinition {
	private int BOM = 0xFEFF;
	
	private static final String testOutputDirectory = System.getProperty("user.home") + "/Desktop";
	private static final String testOutputPath = testOutputDirectory + "/test.html";
	
	@Test
	public void testBuildStructureDefinition() throws FileNotFoundException, IOException, ConfigurationException, DataFormatException, ParserConfigurationException {
		IParser parser = SharedFhirContext.get().newXmlParser();
		try (
			BufferedReader reader = new BufferedReader(new FileReader(getClass().getClassLoader().getResource("example_structure_definition2.xml").getFile()));
		) {
			reader.mark(1);
			int read = reader.read();
			if (read != BOM) {
				System.out.println("First char: " + Integer.toHexString(read));
				reader.reset();
			}
			
			StructureDefinition structureDefinition = (StructureDefinition)parser.parseResource(reader);
			new StructureDefinitionPreparer().prepare(structureDefinition, null);
			SectionedHTMLDoc doc = new SectionedHTMLDoc();
			for (ResourceFormatter<StructureDefinition> formatter : ResourceFormatter.factoryForResource(structureDefinition)) {
				doc.addSection(formatter.makeSectionHTML(structureDefinition));
			}
			
			createOutputDirectory();
			Files.write(Paths.get(testOutputPath), HTMLUtil.docToString(doc.getHTML(), true, false).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
		}
	}
	
	private static void createOutputDirectory() {
		// Create output directory (and parents) if they don't exist
		File directory = new File(testOutputDirectory);
	    if (! directory.exists()){
	        directory.mkdirs();
	    }
	}
}
