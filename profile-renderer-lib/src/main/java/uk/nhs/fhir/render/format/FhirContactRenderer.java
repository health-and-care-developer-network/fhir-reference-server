package uk.nhs.fhir.render.format;

import java.util.List;
import java.util.stream.Collectors;

import org.jdom2.Attribute;
import org.jdom2.Content;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.structdef.FhirContact;
import uk.nhs.fhir.data.structdef.FhirContacts;
import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.style.FhirCSS;

public class FhirContactRenderer extends MetadataListCellRenderer<FhirContacts> {
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
					publishingOrgContacts.addAll(renderSingleTelecom(individualTelecoms));
				} else {
					publishingOrgContacts.addAll(renderMultipleTelecoms(individualTelecoms));
				}
			}
		}
		
		if (!publishingOrgContacts.isEmpty()) {
			publishingOrgContacts.add(0, Elements.withAttributeAndText("span", new Attribute("class", FhirCSS.DATA_LABEL), "Contacts"));
			publishingOrgContacts.add(1, Elements.newElement("br"));
		}
		
		return publishingOrgContacts;
	}

	private List<Content> renderMultipleTelecoms(List<FhirContact> individualTelecoms) {
		List<Content> renderedTelecoms = Lists.newArrayList();
		
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
			renderedTelecoms.add(Elements.newElement("br"));
			
			renderedTelecoms.add(
				Elements.withAttributeAndText("span", 
					new Attribute("class", FhirCSS.DATA_VALUE), 
					"\t" + individualTelecom.getContactData()));
		}
		
		return renderedTelecoms;
	}

	private List<Content> renderSingleTelecom(List<FhirContact> individualTelecoms) {
		List<Content> renderedTelecom = Lists.newArrayList();
		
		renderedTelecom.add(
			Elements.withAttributeAndText("span", 
				new Attribute("class", FhirCSS.TELECOM_NAME), 
				": "));
		
		renderedTelecom.add(
			Elements.withAttributeAndText("span", 
				new Attribute("class", FhirCSS.DATA_VALUE), 
				individualTelecoms.get(0).getContactData()));
		
		return renderedTelecom;
	}
}
