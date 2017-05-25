package uk.nhs.fhir.makehtml.html;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import org.junit.Test;

import uk.nhs.fhir.makehtml.FormattedOutputSpec;
import uk.nhs.fhir.makehtml.HTMLDocSection;
import uk.nhs.fhir.makehtml.ResourceFormatter;
import uk.nhs.fhir.makehtml.prep.StructureDefinitionPreparer;
import uk.nhs.fhir.util.HTMLUtil;
import uk.nhs.fhir.util.SectionedHTMLDoc;
import uk.nhs.fhir.util.SharedFhirContext;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class TestStructureDefinition {
	private int BOM = 0xFEFF;
	
	private static final String testOutputPath = System.getProperty("user.home") + "/Desktop/test.html";
	
	@Test
	public void testBuildStructureDefinition() throws FileNotFoundException, IOException, ConfigurationException, DataFormatException, ParserConfigurationException {
		IParser parser = SharedFhirContext.get().newXmlParser();
		try (
				// TODO KGM 9/May/2017 moved to older strucutre definition example.
			BufferedReader reader = new BufferedReader(new FileReader(getClass().getClassLoader().getResource("example_structure_definition3.xml").getFile()));
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
			for (FormattedOutputSpec formatter : ResourceFormatter.formattersForResource(structureDefinition, "this/path/isnt/used")) {
				HTMLDocSection sectionHTML = formatter.getFormatter().makeSectionHTML(structureDefinition);
				if (sectionHTML != null) {
					doc.addSection(sectionHTML);
				}
			}
			
			Files.write(Paths.get(testOutputPath), HTMLUtil.docToString(doc.getHTML(), true, false).getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
		}
	}
}
