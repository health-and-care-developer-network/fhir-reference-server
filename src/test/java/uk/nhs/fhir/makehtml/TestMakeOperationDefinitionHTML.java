package uk.nhs.fhir.makehtml;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Test;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.model.dstu2.composite.ContactPointDt;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition.Contact;
import ca.uhn.fhir.model.dstu2.valueset.ContactPointSystemEnum;
import ca.uhn.fhir.model.dstu2.valueset.ContactPointUseEnum;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import uk.nhs.fhir.makehtml.old.OperationDefinitionMakerOldStyle;
import uk.nhs.fhir.makehtml.opdef.OperationDefinitionFormatter;
import uk.nhs.fhir.util.HTMLUtil;
import uk.nhs.fhir.util.SectionedHTMLDoc;
import uk.nhs.fhir.util.SharedFhirContext;

public class TestMakeOperationDefinitionHTML {
	@Test
	public void testMakeUsingHapi() throws FileNotFoundException, IOException, ConfigurationException, DataFormatException, ParserConfigurationException, TransformerException {
		IParser parser = SharedFhirContext.get().newXmlParser();
		try (
			FileReader reader = new FileReader(getClass().getClassLoader().getResource("example_operation_definition.xml").getFile());
		) {
			OperationDefinitionFormatter maker = new OperationDefinitionFormatter();
			HTMLDocSection section = maker.makeSectionHTML((OperationDefinition)parser.parseResource(reader));
			SectionedHTMLDoc doc = new SectionedHTMLDoc();
			doc.addSection(section);
			System.out.println(HTMLUtil.docToString(doc.getHTML(), true, false));
		}
	}
	
	@Test
	public void testCreateOperationDefinitionDoc() throws ParserConfigurationException, TransformerException, IOException {
		OperationDefinition def = new OperationDefinition();
		Contact contact1 = new Contact();
		contact1.setName("Interoperability Team");
		ContactPointDt t1 = contact1.addTelecom();
		t1.setSystem(ContactPointSystemEnum.EMAIL);
		t1.setValue("interoperabilityteam@nhs.net");
		t1.setUse(ContactPointUseEnum.WORK);
		def.addContact(contact1);
		
		System.out.println(HTMLUtil.docToString(new OperationDefinitionMakerOldStyle().makeHTML(def), true, false));
	}
}
