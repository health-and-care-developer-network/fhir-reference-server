package uk.nhs.fhir.makehtml;

import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import uk.nhs.fhir.util.FhirDocLinkFactory;

public abstract class ResourceFormatter<T extends IBaseResource> {
	public abstract HTMLDocSection makeSectionHTML(T source) throws ParserConfigurationException;

	protected final FhirDocLinkFactory fhirDocLinkFactory = new FhirDocLinkFactory(FhirContext.forDstu2());
	
	@SuppressWarnings("unchecked")
	public static <T extends IBaseResource> ResourceFormatter<T> factoryForResource(T resource) {
		if (resource instanceof OperationDefinition) {
			return (ResourceFormatter<T>) new OperationDefinitionFormatter();
		}
		
		/*else if (resource instanceof StructureDefinition) {
			return (HTMLSectionFormatter<T>) new StructureDefinitionMakerNEW();
		}*/

		return null;
	}
}