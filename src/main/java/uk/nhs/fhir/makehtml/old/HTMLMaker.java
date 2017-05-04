package uk.nhs.fhir.makehtml.old;

import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jdom2.Document;

import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;

public abstract class HTMLMaker<T extends IBaseResource> {
	public abstract Document makeHTML(T source) throws ParserConfigurationException;

	@SuppressWarnings("unchecked")
	public static <T extends IBaseResource> HTMLMaker<T> factoryForResource(T resource) {
		if (resource instanceof OperationDefinition) {
			return (HTMLMaker<T>) new OperationDefinitionMakerOldStyle();
		}
		
		/*else if (resource instanceof StructureDefinition) {
			return (HTMLMakerNEW<T>) new StructureDefinitionMakerNEW();
		}*/

		return null;
	}
}
