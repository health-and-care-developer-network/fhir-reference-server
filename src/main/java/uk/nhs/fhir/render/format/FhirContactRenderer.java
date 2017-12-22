package uk.nhs.fhir.render.format;

import java.util.List;
import java.util.stream.Collectors;

import org.jdom2.Attribute;
import org.jdom2.Content;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.structdef.FhirContact;
import uk.nhs.fhir.data.structdef.FhirContacts;
import uk.nhs.fhir.render.html.jdom2.Elements;
import uk.nhs.fhir.render.html.style.FhirCSS;

public class FhirContactRenderer {
	public List<Content> getPublishingOrgContactsContents(List<FhirContacts> contacts) {
		
		List<Content> publishingOrgContacts = Lists.newArrayList();
		
		for (FhirContacts contact : contacts) {
			List<FhirContact> individualTelecoms = contact.getTelecoms();
			if (!individualTelecoms.isEmpty()) {
				
				newlineIfNeeded(publishingOrgContacts);
				
				String telecomDesc = contact.getName().orElse("General");
				
				publishingOrgContacts.add(
					Elements.withAttributeAndText("span", 
						new Attribute("class", FhirCSS.TELECOM_NAME), 
						telecomDesc));
				
				if (individualTelecoms.size() == 1) {
					renderSingleTelecom(publishingOrgContacts, individualTelecoms);
				} else {
					renderMultipleTelecoms(publishingOrgContacts, individualTelecoms);
				}
			}
		}
		
		if (!publishingOrgContacts.isEmpty()) {
			publishingOrgContacts.add(0, Elements.withAttributeAndText("span", new Attribute("class", FhirCSS.DATA_LABEL), "Contacts"));
			publishingOrgContacts.add(1, Elements.newElement("br"));
		}
		
		return publishingOrgContacts;
	}

	void renderMultipleTelecoms(List<Content> publishingOrgContacts, List<FhirContact> individualTelecoms) {
		List<FhirContact> contactsByPrecedence = 
			individualTelecoms
				.stream()
				// Precedence is more important if number is lower
				// so if precedence is not present, treat it as a very big number (i.e. less important)
				.sorted((contact1, contact2) -> 
				!contact1.getPrecedence().isPresent() && !contact2.getPrecedence().isPresent() ? 0 :
					!contact1.getPrecedence().isPresent() ? 1 : 
					!contact2.getPrecedence().isPresent() ? -1 : 
					Integer.compare(contact1.getPrecedence().get(), contact2.getPrecedence().get()))
				.collect(Collectors.toList()); 
		
		for (FhirContact individualTelecom : contactsByPrecedence) {
			publishingOrgContacts.add(Elements.newElement("br"));
			
			publishingOrgContacts.add(
				Elements.withAttributeAndText("span", 
					new Attribute("class", FhirCSS.DATA_VALUE), 
					"\t" + individualTelecom.getContactData()));
		}
	}

	void renderSingleTelecom(List<Content> publishingOrgContacts, List<FhirContact> individualTelecoms) {
		publishingOrgContacts.add(
			Elements.withAttributeAndText("span", 
				new Attribute("class", FhirCSS.TELECOM_NAME), 
				": "));
		
		publishingOrgContacts.add(
			Elements.withAttributeAndText("span", 
				new Attribute("class", FhirCSS.DATA_VALUE), 
				individualTelecoms.get(0).getContactData()));
	}

	void newlineIfNeeded(List<Content> publishingOrgContacts) {
		if (!publishingOrgContacts.isEmpty()) {
			publishingOrgContacts.add(Elements.newElement("br"));
		}
	}
}
