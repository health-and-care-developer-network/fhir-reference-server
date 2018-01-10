package uk.nhs.fhir.data.wrap;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import ca.uhn.fhir.model.dstu2.composite.ContactPointDt;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition.Contact;
import ca.uhn.fhir.model.dstu2.valueset.ConformanceResourceStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.ExtensionContextEnum;
import ca.uhn.fhir.model.dstu2.valueset.StructureDefinitionKindEnum;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import junit.framework.Assert;
import uk.nhs.fhir.data.structdef.FhirContact;
import uk.nhs.fhir.data.structdef.FhirContacts;
import uk.nhs.fhir.data.wrap.dstu2.WrappedDstu2StructureDefinition;
import uk.nhs.fhir.util.FhirVersion;

public class TestWrappedDstu2StructureDefinition {
	
	@Test
	public void testMetadataRetrieval() {
		String name = "test-name";
		String url = "http://test-url";
		StructureDefinitionKindEnum kindEnum = StructureDefinitionKindEnum.DATA_TYPE;
		String kind = "datatype";
		ConformanceResourceStatusEnum statusEnum = ConformanceResourceStatusEnum.ACTIVE;
		String status = "active";
		Boolean isAbstract = Boolean.FALSE;
		String constrainedType = "test-type";
		String base = "http://test-base";
		String version = "1.1.1";
		String display = "test-display";
		String publisher = "test-publisher";
		String description = "test-description";
		
		@SuppressWarnings("deprecation")
		Date date = new Date(2000, 1, 1, 10, 10, 10);
		DateTimeDt fhirDate = new DateTimeDt(date);
		
		String copyright = "test-copyright";
		String fhirVersion = "3.0.1";
		String resolvedFhirVersion = "STU3";
		ExtensionContextEnum contextTypeEnum = ExtensionContextEnum.DATATYPE;
		String contextType = "datatype";
		
		StructureDefinition sd = new StructureDefinition();
		sd.setName(name);
		sd.setUrl(url);
		sd.setKind(kindEnum);
		sd.setStatus(statusEnum);
		sd.setAbstract(isAbstract);
		sd.setConstrainedType(constrainedType);
		sd.setBase(base);
		sd.setVersion(version);
		sd.setDisplay(display);
		sd.setPublisher(publisher);
		sd.setDate(fhirDate);
		sd.setDescription(description);
		sd.setCopyright(copyright);
		sd.setFhirVersion(fhirVersion);
		sd.setContextType(contextTypeEnum);

		WrappedStructureDefinition definition = new WrappedDstu2StructureDefinition(sd);
		Assert.assertEquals(FhirVersion.DSTU2, definition.getImplicitFhirVersion());
		Assert.assertEquals(name, definition.getName());
		Assert.assertEquals(url, definition.getUrl().get());
		Assert.assertEquals(kind, definition.getKind());
		Assert.assertEquals(status, definition.getStatus());
		Assert.assertEquals(isAbstract, definition.getAbstract());
		Assert.assertEquals(constrainedType, definition.getConstrainedType().get());
		Assert.assertEquals(base, definition.getBase());
		Assert.assertEquals(version, definition.getVersion().get());
		Assert.assertEquals(display, definition.getDisplay().get());
		Assert.assertEquals(publisher, definition.getPublisher().get());
		Assert.assertEquals(date, definition.getDate().get());
		Assert.assertEquals(description, definition.getDescription().get());
		Assert.assertEquals(copyright, definition.getCopyright().get());
		Assert.assertEquals(resolvedFhirVersion, definition.getFhirVersion().get());
		Assert.assertEquals(contextType, definition.getContextType().get());
	}
	
	public void test1Contact1Telecom() {
		List<Contact> contacts = Lists.newArrayList(
			new Contact()
				.setName("name1")
				.setTelecom(Lists.newArrayList(
					new ContactPointDt().setValue("details").setRank(1)))
		);
		StructureDefinition sd = new StructureDefinition().setContact(contacts);
		WrappedStructureDefinition definition = new WrappedDstu2StructureDefinition(sd);
		
		FhirContacts contacts1 = new FhirContacts("name1");
		contacts1.addTelecom(new FhirContact("details", 1));
		
		List<FhirContacts> expectedContacts = Lists.newArrayList(contacts1);
		
		Assert.assertEquals(expectedContacts, definition.getContacts());
	}
	
	@Test
	public void test1Contact2Telecoms() {
		List<Contact> contacts = Lists.newArrayList(
			new Contact()
				.setName("name1")
				.setTelecom(Lists.newArrayList(
					new ContactPointDt().setValue("details").setRank(1),
					new ContactPointDt().setValue("details2")))
		);
		StructureDefinition sd = new StructureDefinition().setContact(contacts);
		WrappedStructureDefinition definition = new WrappedDstu2StructureDefinition(sd);
		
		FhirContacts contacts1 = new FhirContacts("name1");
		contacts1.addTelecom(new FhirContact("details", 1));
		contacts1.addTelecom(new FhirContact("details2"));
		
		List<FhirContacts> expectedContacts = Lists.newArrayList(contacts1);
		
		Assert.assertEquals(expectedContacts, definition.getContacts());
	}
	
	@Test
	public void test2ContactsManyTelecoms() {
		List<Contact> contacts = Lists.newArrayList(
			new Contact()
				.setName("name1")
				.setTelecom(Lists.newArrayList(
					new ContactPointDt().setValue("details").setRank(1),
					new ContactPointDt().setValue("details2"))),
			new Contact()
				.setName("name2")
				.setTelecom(Lists.newArrayList(
					new ContactPointDt().setValue("details3"),
					new ContactPointDt().setValue("details4").setRank(2),
					new ContactPointDt().setValue("details5").setRank(3)))
		);
		StructureDefinition sd = new StructureDefinition().setContact(contacts);
		WrappedStructureDefinition definition = new WrappedDstu2StructureDefinition(sd);

		FhirContacts contacts1 = new FhirContacts("name1");
		contacts1.addTelecom(new FhirContact("details", 1));
		contacts1.addTelecom(new FhirContact("details2"));
		FhirContacts contacts2 = new FhirContacts("name2");
		contacts2.addTelecom(new FhirContact("details3"));
		contacts2.addTelecom(new FhirContact("details4", 2));
		contacts2.addTelecom(new FhirContact("details5", 3));
		
		List<FhirContacts> expectedContacts = Lists.newArrayList(contacts1, contacts2);
		
		Assert.assertEquals(expectedContacts, definition.getContacts());
	}
}
