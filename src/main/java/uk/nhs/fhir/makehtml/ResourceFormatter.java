package uk.nhs.fhir.makehtml;

import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import com.google.common.collect.Lists;
import org.hl7.fhir.instance.model.api.IBaseResource;
import uk.nhs.fhir.makehtml.data.ResourceSectionType;
import uk.nhs.fhir.makehtml.opdef.OperationDefinitionFormatter;
import uk.nhs.fhir.util.FhirDocLinkFactory;

import javax.xml.parsers.ParserConfigurationException;
import java.util.List;

// KGM 13/Apr/2017 Added ValueSet

public abstract class ResourceFormatter<T extends IBaseResource> {
	public abstract HTMLDocSection makeSectionHTML(T source) throws ParserConfigurationException;

	public ResourceSectionType resourceSectionType = ResourceSectionType.TREEVIEW;

	protected final FhirDocLinkFactory fhirDocLinkFactory = new FhirDocLinkFactory();
	
	@SuppressWarnings("unchecked")
	public static <T extends IBaseResource> List<ResourceFormatter<T>> factoryForResource(T resource) {
		if (resource instanceof OperationDefinition) {
			return Lists.newArrayList(
				(ResourceFormatter<T>) new OperationDefinitionFormatter());
		} else if (resource instanceof StructureDefinition) {
			
			/*
			ArrayList<ResourceFormatter<T>> structureDefinitionFormatters = Lists.newArrayList(
				(ResourceFormatter<T>) new StructureDefinitionMetadataFormatter(),
				(ResourceFormatter<T>) new StructureDefinitionProfileFormatter());
			

			StructureDefinition sd = (StructureDefinition)resource;
			if (!sd.getConstrainedType().equals("Extension")) {
				structureDefinitionFormatters.add(
					(ResourceFormatter<T>) new StructureDefinitionDifferentialFormatter());
			}

			return structureDefinitionFormatters;*/
			
			return Lists.newArrayList((ResourceFormatter<T>) new StructureDefinitionProfileFormatter(),(ResourceFormatter<T>) new StructureDefinitionBindingFormatter());
			
		} else if (resource instanceof ValueSet) {
			return Lists.newArrayList((ResourceFormatter<T>) new ValueSetFormatter());
		}

		return null;
	}
}