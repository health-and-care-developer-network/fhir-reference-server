package uk.nhs.fhir.makehtml;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;

public class ResourceBuilder {
	
	private static final Logger LOG = Logger.getLogger(NewMain.class.getName());
	
	protected static String addTextSectionToResource(String resourceXML, String textSection) {
        FhirContext ctx = FhirContext.forDstu2();
        StructureDefinition structureDefinitionResource = null;
        
        structureDefinitionResource = (StructureDefinition) ctx.newXmlParser().parseResource(resourceXML);
        NarrativeDt textElement = new NarrativeDt();
        textElement.setStatus(NarrativeStatusEnum.GENERATED);
        textElement.setDiv(textSection);
        structureDefinitionResource.setText(textElement);
        
        String serialised = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(structureDefinitionResource);
        return serialised;
	}
	
}
