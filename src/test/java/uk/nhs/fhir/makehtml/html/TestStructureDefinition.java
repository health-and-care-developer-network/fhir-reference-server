package uk.nhs.fhir.makehtml.html;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Ignore;
import org.junit.Test;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import uk.nhs.fhir.makehtml.FormattedOutputSpec;
import uk.nhs.fhir.makehtml.data.wrap.WrappedResource;
import uk.nhs.fhir.makehtml.html.jdom2.HTMLUtil;
import uk.nhs.fhir.makehtml.prep.StructureDefinitionPreparer;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;
import uk.nhs.fhir.makehtml.render.SectionedHTMLDoc;
import uk.nhs.fhir.util.HAPIUtils;

public class TestStructureDefinition {
	private int BOM = 0xFEFF;
	
	private static final String testOutputPath = System.getProperty("user.home") + "/Desktop/test.html";
	
	@Ignore
	@Test
	public void testBuildStructureDefinition() throws FileNotFoundException, IOException, ConfigurationException, DataFormatException, ParserConfigurationException {
		IParser parser = HAPIUtils.dstu2Context().newXmlParser();
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
			WrappedResource wrappedStructureDefinition = WrappedResource.fromBaseResource(structureDefinition);
			for (FormattedOutputSpec formatter : ResourceFormatter.formattersForResource(wrappedStructureDefinition, "this/path/isnt/used")) {
				HTMLDocSection sectionHTML = formatter.getFormatter().makeSectionHTML(wrappedStructureDefinition);
				if (sectionHTML != null) {
					doc.addSection(sectionHTML);
				}
			}
			
			Files.write(Paths.get(testOutputPath), HTMLUtil.docToString(doc.getHTML(), true, false).getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		}
	}
}
