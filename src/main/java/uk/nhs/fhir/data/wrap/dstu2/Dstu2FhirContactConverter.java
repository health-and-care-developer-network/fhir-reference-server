package uk.nhs.fhir.data.wrap.dstu2;

import java.util.List;

import com.google.common.collect.Lists;

import ca.uhn.fhir.model.dstu2.composite.ContactPointDt;
import ca.uhn.fhir.model.dstu2.resource.ValueSet.Contact;
import uk.nhs.fhir.data.structdef.FhirContact;
import uk.nhs.fhir.data.structdef.FhirContacts;

public class Dstu2FhirContactConverter {

	public List<FhirContacts> convertList(List<Contact> contacts) {
		List<FhirContacts> newContacts = Lists.newArrayList();
		
		for (Contact contact : contacts) {
			newContacts.add(convert(contact));
		}
		
		return newContacts;
	}
	
	public FhirContacts convert(Contact contact) {
		FhirContacts fhirContact = new FhirContacts(contact.getName());
		
		for (ContactPointDt telecom : contact.getTelecom()){
			String value = telecom.getValue();
			int rank = telecom.getRank();
			fhirContact.addTelecom(new FhirContact(value, rank));
		}
		
		return fhirContact;
	}
}