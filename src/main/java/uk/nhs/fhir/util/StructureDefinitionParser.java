package uk.nhs.fhir.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.parser.IParser;

public class StructureDefinitionParser {
	private static final int BOM = 0xFEFF;
	
	public StructureDefinition parseStructureDefinition(File profile) {
		IParser parser = SharedFhirContext.get().newXmlParser();
		try (
			BufferedReader reader = new BufferedReader(new FileReader(profile));
		) {
			reader.mark(1);
			int read = reader.read();
			if (read != BOM) {
				System.out.println("First char: " + Integer.toHexString(read));
				reader.reset();
			}
			
			return (StructureDefinition) parser.parseResource(reader);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
