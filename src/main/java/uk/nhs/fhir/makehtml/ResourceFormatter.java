package uk.nhs.fhir.makehtml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.collect.Lists;

import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import uk.nhs.fhir.makehtml.html.StructureDefinitionMetadataFormatter;
import uk.nhs.fhir.makehtml.opdef.OperationDefinitionFormatter;
import uk.nhs.fhir.util.FhirDocLinkFactory;

public abstract class ResourceFormatter<T extends IBaseResource> {
	public abstract HTMLDocSection makeSectionHTML(T source) throws ParserConfigurationException;

	protected final FhirDocLinkFactory fhirDocLinkFactory = new FhirDocLinkFactory();
	
	@SuppressWarnings("unchecked")
	public static <T extends IBaseResource> List<ResourceFormatter<T>> factoryForResource(T resource) {
		if (resource instanceof OperationDefinition) {
			return Lists.newArrayList(
				(ResourceFormatter<T>) new OperationDefinitionFormatter());
		} else if (resource instanceof StructureDefinition) {
			ArrayList<ResourceFormatter<T>> structureDefinitionFormatters = Lists.newArrayList(
				(ResourceFormatter<T>) new StructureDefinitionMetadataFormatter(),
				(ResourceFormatter<T>) new StructureDefinitionProfileFormatter());

			StructureDefinition sd = (StructureDefinition)resource;
			if (!sd.getConstrainedType().equals("Extension")) {
				structureDefinitionFormatters.add(
					(ResourceFormatter<T>) new StructureDefinitionDifferentialFormatter());
			}
			
			return structureDefinitionFormatters;
		}

		return null;
	}
}