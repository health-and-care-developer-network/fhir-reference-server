package uk.nhs.fhir.makehtml;

import java.util.List;
import java.util.Optional;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
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
import uk.nhs.fhir.makehtml.data.FhirTreeBuilder;
import uk.nhs.fhir.makehtml.data.FhirTreeData;

public class StructureDefinitionTreeDataProvider {
	
	private final StructureDefinition source;
	
	public StructureDefinitionTreeDataProvider(StructureDefinition source) {
		this.source = source;
	}
	
	public StructureDefinitionMetadata getMetaData() {
		StringDt name = source.getNameElement();
		UriDt url = source.getUrlElement();
		
		String type;
		CodeDt constrainedType = source.getConstrainedTypeElement();
		if (!constrainedType.isEmpty()) {
			type = constrainedType.getValueAsString();
		} else {
			type = "?";
		}
		
		UriDt base = source.getBaseElement();
		Optional<String> baseTypeUrl;
		if (!base.isEmpty()) {
			String origBaseUrl = base.getValue();
			String dstu2BaseUrl = "http://www.hl7.org/fhir/DSTU2" + origBaseUrl.substring(origBaseUrl.lastIndexOf('/'), origBaseUrl.length());
			baseTypeUrl = Optional.of(dstu2BaseUrl);
		} else {
			baseTypeUrl = Optional.empty();
		}
		
		List<IdentifierDt> identifiers = source.getIdentifier();
		for (IdentifierDt identifier : identifiers) {
			
		}
		
		StringDt version = source.getVersionElement();
		if (!version.isEmpty()) {
			
		}
		
		BoundCodeDt<StructureDefinitionKindEnum> kind = source.getKindElement();
		
		StringDt display = source.getDisplayElement();
		if (!display.isEmpty()) {
			
		}
		
		ConformanceResourceStatusEnum resourceStatus = source.getStatusElement().getValueAsEnum();
		
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
		
		BooleanDt abstractElement = source.getAbstractElement();
		
		BoundCodeDt<ExtensionContextEnum> contextType = source.getContextTypeElement();
		if (!contextType.isEmpty()) {
			
		}
		
		List<StringDt> contexts = source.getContext();
		for (StringDt context : contexts) {
			
		}
		
		return new StructureDefinitionMetadata();
	}
	
	public FhirTreeData getTreeData() {

		
		
		FhirTreeBuilder fhirTreeBuilder = new FhirTreeBuilder();
		
		Snapshot snapshot = source.getSnapshot();
		List<ElementDefinitionDt> snapshotElements = snapshot.getElement();
		for (ElementDefinitionDt elementDefinition : snapshotElements) {
			fhirTreeBuilder.addElementDefinition(elementDefinition);
		}
		
		FhirTreeData tree = fhirTreeBuilder.getTree();
		
		Differential differential = source.getDifferential();
		List<ElementDefinitionDt> differentialElements = differential.getElement();
		for (ElementDefinitionDt differentialElement : differentialElements) {
			// min/max are optional, and default back to the base if not present
		}
		
		return tree;
	}
}
