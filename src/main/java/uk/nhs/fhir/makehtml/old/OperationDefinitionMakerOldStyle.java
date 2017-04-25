package uk.nhs.fhir.makehtml.old;

import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Document;

import com.google.common.collect.Lists;

import ca.uhn.fhir.model.dstu2.composite.ContactPointDt;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition.Contact;
import ca.uhn.fhir.model.dstu2.valueset.ResourceTypeEnum;
import ca.uhn.fhir.model.primitive.BoundCodeDt;
import uk.nhs.fhir.makehtml.data.LinkData;
import uk.nhs.fhir.makehtml.data.SimpleLinkData;

public class OperationDefinitionMakerOldStyle extends HTMLMaker<OperationDefinition> {

	@Override
	public Document makeHTML(OperationDefinition operationDefinition) throws ParserConfigurationException {
		FormattedTableHTML table = new FormattedTableHTML();
		
		table.titles("Name", "Value");
		addContactSection(table, operationDefinition.getContact());
		addMetaDataSection(table, operationDefinition);
		addTypeSection(table, operationDefinition.getType());
		
		return new Document(table.generateHTML());
	}

	private void addContactSection(FormattedTableHTML table, List<Contact> contacts) {
		if (!contacts.isEmpty()) {
			table.sectionStart("Contacts");
			
			for (Contact contact : contacts) {
				table.dataRow(contact.getName(), "Name", "Name of a individual to contact");
				for (ContactPointDt telecom : contact.getTelecom()) {
					table.dataRow(telecom.getSystem(), "Type", "phone | fax | email | pager | other");
					table.dataRow(telecom.getValue(), "Value", "The actual contact point details");
					table.dataRow(telecom.getUse(), "Use type", "home | work | temp | old | mobile - purpose of this contact point");
				}
			}
			
			table.borderedRow();
		}	
	}

	private void addMetaDataSection(FormattedTableHTML table, OperationDefinition operationDefinition) {
		table.dataIfNotNull(operationDefinition.getDate(), "Date", "Date for this version of the operation definition");
		table.dataRow(operationDefinition.getDescription(), "Description", "Natural language description of the operation");
		table.dataRow(operationDefinition.getRequirements(), "Requirements", "Why is this needed?");
		table.dataIfNotNull(operationDefinition.getIdempotent(), "Is idempotent", "Whether content is unchanged by operation");
		table.dataRow(operationDefinition.getCode(), "Code", "Name used to invoke the operation");
		table.dataRow(operationDefinition.getNotes(), "Notes", "Additional information about use");
		table.dataIfNotNull(operationDefinition.getBase().getReference(), "Base", "Marks this as a profile of the base");
		table.dataIfNotNull(operationDefinition.getSystem(), "System", "Invoke at the system level?");
	}

	private void addTypeSection(FormattedTableHTML table, List<BoundCodeDt<ResourceTypeEnum>> types) {
		if (!types.isEmpty()) {
			List<LinkData> links = Lists.newArrayList();;
			for (BoundCodeDt<ResourceTypeEnum> type : types) {
				String code = type.getValueAsString();
				System.out.println(code);
				if (code.equals("DomainResource")) {
					links.add(new SimpleLinkData("https://www.hl7.org/fhir/domainresource.html", code));
				} else if (Arrays.asList(Constants.BASERESOURCETYPES).contains(code)) {
					links.add(new SimpleLinkData("https://www.hl7.org/fhir/datatypes.html#" + code, code));
				} else if (Arrays.asList(Constants.RESOURCETYPES).contains(code)) {
					links.add(new SimpleLinkData("https://www.hl7.org/fhir/" + code + ".html", code));
				} else {
					links.add(new SimpleLinkData("#", code));
				}
			}
			table.dataRow(links, "Type", "Invoke at resource level for these types");
		}
	}
}
