package uk.nhs.fhir.makehtml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.collect.Lists;

import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import uk.nhs.fhir.makehtml.data.ResourceSectionType;
import uk.nhs.fhir.makehtml.opdef.OperationDefinitionFormatter;
import uk.nhs.fhir.makehtml.structdef.StructureDefinitionBindingFormatter;
import uk.nhs.fhir.makehtml.structdef.StructureDefinitionDifferentialFormatter;
import uk.nhs.fhir.makehtml.structdef.StructureDefinitionMetadataFormatter;
import uk.nhs.fhir.makehtml.structdef.StructureDefinitionSnapshotFormatter;
import uk.nhs.fhir.util.FhirDocLinkFactory;

// KGM 13/Apr/2017 Added ValueSet

public abstract class ResourceFormatter {
	public abstract HTMLDocSection makeSectionHTML(IBaseResource source) throws ParserConfigurationException;

	public ResourceSectionType resourceSectionType = ResourceSectionType.TREEVIEW;

	protected final FhirDocLinkFactory fhirDocLinkFactory = new FhirDocLinkFactory();
	
	public static List<FormattedOutputSpec> formattersForResource(IBaseResource resource, String baseOutputDirectory) {
		if (resource instanceof OperationDefinition) {
			return Lists.newArrayList(
				new FormattedOutputSpec(resource, new OperationDefinitionFormatter(), baseOutputDirectory, "render"));
		} else if (resource instanceof StructureDefinition) {
			
			ArrayList<FormattedOutputSpec> structureDefinitionFormatters = Lists.newArrayList(
				new FormattedOutputSpec(resource, new StructureDefinitionMetadataFormatter(), baseOutputDirectory, "metadata"),
				new FormattedOutputSpec(resource, new StructureDefinitionSnapshotFormatter(), baseOutputDirectory, "snapshot"),
				new FormattedOutputSpec(resource, new StructureDefinitionBindingFormatter(), baseOutputDirectory, "bindings"));
			

			StructureDefinition sd = (StructureDefinition)resource;
			if (!sd.getConstrainedType().equals("Extension")) {
				structureDefinitionFormatters.add(
					new FormattedOutputSpec(resource, new StructureDefinitionDifferentialFormatter(), baseOutputDirectory, "differential"));
			}

			return structureDefinitionFormatters;
			
			//return Lists.newArrayList((ResourceFormatter<T>) new StructureDefinitionProfileFormatter());
			
		} else if (resource instanceof ValueSet) {
			return Lists.newArrayList(
				new FormattedOutputSpec(resource, new ValueSetFormatter(), baseOutputDirectory, "render"));
		}

		return null;
	}
}