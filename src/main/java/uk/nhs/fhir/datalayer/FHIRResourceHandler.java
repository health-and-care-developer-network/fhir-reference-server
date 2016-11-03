package uk.nhs.fhir.datalayer;

import java.io.File;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.parser.DataFormatException;
import uk.nhs.fhir.util.FileLoader;

public class FHIRResourceHandler {
	
	private static FhirContext ctx = FhirContext.forDstu2();
	
	
	public static StructureDefinition loadProfileFromFile(final String filename) {
        return loadProfileFromFile(new File(filename));
    }
	
	public static StructureDefinition loadProfileFromFile(final File file) {
		String resource = FileLoader.loadFile(file);
        StructureDefinition profile = null;
        try {
        	profile =
        			(StructureDefinition) ctx.newXmlParser().parseResource(resource);
        } catch (ConfigurationException e) {
        	e.printStackTrace();
        } catch (DataFormatException e) {
        	e.printStackTrace();
        }
        return profile;
	}
}
