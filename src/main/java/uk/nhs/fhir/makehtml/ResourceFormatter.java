package uk.nhs.fhir.makehtml;

import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import org.hl7.fhir.instance.model.api.IBaseResource;
import uk.nhs.fhir.makehtml.opdef.OperationDefinitionFormatter;
import uk.nhs.fhir.util.FhirDocLinkFactory;

import javax.xml.parsers.ParserConfigurationException;

// KGM 13/Apr/2017 Added ValueSet

public abstract class ResourceFormatter<T extends IBaseResource> {
	public abstract HTMLDocSection makeSectionHTML(T source) throws ParserConfigurationException;

	protected final FhirDocLinkFactory fhirDocLinkFactory = new FhirDocLinkFactory();
	
	@SuppressWarnings("unchecked")
	public static <T extends IBaseResource> ResourceFormatter<T> factoryForResource(T resource) {
		if (resource instanceof OperationDefinition) {
			return (ResourceFormatter<T>) new OperationDefinitionFormatter();
		} else if (resource instanceof StructureDefinition) {
			return (ResourceFormatter<T>) new StructureDefinitionFormatter();
		} else if (resource instanceof ValueSet) {
			return (ResourceFormatter<T>) new ValueSetFormatter();
		}

		return null;
	}
}