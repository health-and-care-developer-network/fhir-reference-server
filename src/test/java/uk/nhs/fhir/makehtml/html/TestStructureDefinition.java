package uk.nhs.fhir.makehtml.html;

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
import uk.nhs.fhir.makehtml.HTMLDocSection;
import uk.nhs.fhir.makehtml.StructureDefinitionFormatter;
import uk.nhs.fhir.util.HTMLUtil;
import uk.nhs.fhir.util.SectionedHTMLDoc;
import uk.nhs.fhir.util.SharedFhirContext;

public class TestStructureDefinition {
	@Test
	public void testBuildStructureDefinition() throws FileNotFoundException, IOException, ConfigurationException, DataFormatException, ParserConfigurationException {
		IParser parser = SharedFhirContext.get().newXmlParser();
		try (
			FileReader reader = new FileReader(getClass().getClassLoader().getResource("example_structure_definition.xml").getFile());
		) {
			StructureDefinitionFormatter maker = new StructureDefinitionFormatter();
			StructureDefinition structureDefinition = (StructureDefinition)parser.parseResource(reader);
			HTMLDocSection section = maker.makeSectionHTML(structureDefinition);
			SectionedHTMLDoc doc = new SectionedHTMLDoc();
			doc.addSection(section);
			Files.write(Paths.get("/home/jon/Desktop/test.html"), HTMLUtil.docToString(doc.getHTML(), true, false).getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
		}
	}
}
