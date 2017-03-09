package uk.nhs.fhir.makehtml;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition.Contact;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition.Differential;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition.Mapping;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition.Snapshot;
import ca.uhn.fhir.model.dstu2.valueset.ConformanceResourceStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.ExtensionContextEnum;
import ca.uhn.fhir.model.dstu2.valueset.StructureDefinitionKindEnum;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.BoundCodeDt;
import ca.uhn.fhir.model.primitive.CodeDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.model.primitive.UriDt;

public class StructureDefinitionFormatter extends ResourceFormatter<StructureDefinition> {

	@Override
	public HTMLDocSection makeSectionHTML(StructureDefinition source) throws ParserConfigurationException {
		
		UriDt url = source.getUrlElement();
		
		List<IdentifierDt> identifiers = source.getIdentifier();
		for (IdentifierDt identifier : identifiers) {
			
		}
		
		StringDt version = source.getVersionElement();
		if (!version.isEmpty()) {
			
		}
		
		StringDt name = source.getNameElement();
		
		StringDt display = source.getDisplayElement();
		if (!display.isEmpty()) {
			
		}
		
		ConformanceResourceStatusEnum valueAsEnum = source.getStatusElement().getValueAsEnum();
		
		BooleanDt isExperimental = source.getExperimentalElement();
		if (!isExperimental.isEmpty()) {
			
		}
		
		StringDt publisher = source.getPublisherElement();
		if (!publisher.isEmpty()) {
			
		}
		
		List<Contact> contacts = source.getContact();
		for (Contact contact : contacts) {
			
		}
		
		DateTimeDt date = source.getDateElement();
		if (!date.isEmpty()) {
			
		}
		
		StringDt description = source.getDescriptionElement();
		if (!description.isEmpty()) {
			
		}
		
		List<CodeableConceptDt> useContexts = source.getUseContext();
		for (CodeableConceptDt useContext : useContexts) {
			
		}
		
		StringDt requirements = source.getRequirementsElement();
		if (!requirements.isEmpty()) {
			
		}
		
		StringDt copyright = source.getCopyrightElement();
		if (!copyright.isEmpty()) {
			
		}
		
		List<CodingDt> codes = source.getCode();
		for (CodingDt code : codes) {
			
		}
		
		IdDt fhirVersion = source.getFhirVersionElement();
		if (!fhirVersion.isEmpty()) {
			
		}
		
		List<Mapping> mappings = source.getMapping();
		for (Mapping mapping : mappings) {
			
		}
		
		BoundCodeDt<StructureDefinitionKindEnum> kind = source.getKindElement();
		
		CodeDt constrainedType = source.getConstrainedTypeElement();
		if (!constrainedType.isEmpty()) {
			
		}
		
		BooleanDt abstractElement = source.getAbstractElement();
		
		BoundCodeDt<ExtensionContextEnum> contextType = source.getContextTypeElement();
		if (!contextType.isEmpty()) {
			
		}
		
		List<StringDt> contexts = source.getContext();
		for (StringDt context : contexts) {
			
		}
		
		UriDt base = source.getBaseElement();
		if (!base.isEmpty()) {
			
		}
		
		Snapshot snapshot = source.getSnapshot();
		
		Differential differential = source.getDifferential();
		
		HTMLDocSection structureDefinitionSection = new HTMLDocSection();
		
		return structureDefinitionSection;
	}

}
