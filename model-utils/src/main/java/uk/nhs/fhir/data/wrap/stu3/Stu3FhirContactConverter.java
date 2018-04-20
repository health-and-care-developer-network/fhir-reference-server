package uk.nhs.fhir.data.wrap.stu3;

import java.util.List;

import org.hl7.fhir.dstu3.model.ContactDetail;
import org.hl7.fhir.dstu3.model.ContactPoint;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.structdef.FhirContact;
import uk.nhs.fhir.data.structdef.FhirContacts;

public class Stu3FhirContactConverter {

	public List<FhirContacts> convertList(List<ContactDetail> contacts) {
		List<FhirContacts> newContacts = Lists.newArrayList();
		
		for (ContactDetail contact : contacts) {
			newContacts.add(convert(contact));
		}
		
		return newContacts;
	}
	
	public FhirContacts convert(ContactDetail contact) {
		FhirContacts fhirContact = new FhirContacts(contact.getName());
		
		for (ContactPoint telecom : contact.getTelecom()){
			String value = telecom.getValue();
			int rank = telecom.getRank();
			fhirContact.addTelecom(new FhirContact(value, rank));
		}
		
		return fhirContact;
	}
}
