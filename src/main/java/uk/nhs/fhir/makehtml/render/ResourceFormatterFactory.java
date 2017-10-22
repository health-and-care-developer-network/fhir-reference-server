package uk.nhs.fhir.makehtml.render;

import java.nio.file.Path;
import java.util.List;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.wrap.WrappedCodeSystem;
import uk.nhs.fhir.data.wrap.WrappedConceptMap;
import uk.nhs.fhir.data.wrap.WrappedOperationDefinition;
import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.data.wrap.WrappedValueSet;
import uk.nhs.fhir.makehtml.FhirFileRegistry;
import uk.nhs.fhir.makehtml.FormattedOutputSpec;
import uk.nhs.fhir.makehtml.render.codesys.CodeSystemConceptTableFormatter;
import uk.nhs.fhir.makehtml.render.codesys.CodeSystemFiltersTableFormatter;
import uk.nhs.fhir.makehtml.render.codesys.CodeSystemFormatter;
import uk.nhs.fhir.makehtml.render.codesys.CodeSystemMetadataFormatter;
import uk.nhs.fhir.makehtml.render.conceptmap.ConceptMapFormatter;
import uk.nhs.fhir.makehtml.render.conceptmap.ConceptMapMetadataFormatter;
import uk.nhs.fhir.makehtml.render.conceptmap.ConceptMapTableFormatter;
import uk.nhs.fhir.makehtml.render.opdef.OperationDefinitionFormatter;
import uk.nhs.fhir.makehtml.render.structdef.StructureDefinitionBindingFormatter;
import uk.nhs.fhir.makehtml.render.structdef.StructureDefinitionDetailsFormatter;
import uk.nhs.fhir.makehtml.render.structdef.StructureDefinitionDifferentialFormatter;
import uk.nhs.fhir.makehtml.render.structdef.StructureDefinitionFormatter;
import uk.nhs.fhir.makehtml.render.structdef.StructureDefinitionMetadataFormatter;
import uk.nhs.fhir.makehtml.render.structdef.StructureDefinitionSnapshotFormatter;
import uk.nhs.fhir.makehtml.render.valueset.ValueSetFormatter;
import uk.nhs.fhir.util.SimpleFhirFileLocator;

public class ResourceFormatterFactory {
	public ResourceFormatter<?> defaultFormatter(WrappedResource<?> wrappedResource, FhirFileRegistry otherResources) {
		if (wrappedResource instanceof WrappedConceptMap) {
			return new ConceptMapFormatter((WrappedConceptMap) wrappedResource, otherResources);
		} else if (wrappedResource instanceof WrappedCodeSystem) {
			return new CodeSystemFormatter((WrappedCodeSystem) wrappedResource, otherResources);
		} else if (wrappedResource instanceof WrappedOperationDefinition) {
			return new OperationDefinitionFormatter((WrappedOperationDefinition) wrappedResource, otherResources);
		} else if (wrappedResource instanceof WrappedValueSet) {
			return new ValueSetFormatter((WrappedValueSet) wrappedResource, otherResources);
		} else if (wrappedResource instanceof WrappedStructureDefinition) {
			return new StructureDefinitionFormatter((WrappedStructureDefinition) wrappedResource, otherResources);
		} else {
			throw new IllegalStateException("Unexpected wrapped resource class " + wrappedResource.getClass().getName());
		}
	}
	
	public List<FormattedOutputSpec<?>> allFormatterSpecs(WrappedResource<?> wrappedResource, SimpleFhirFileLocator renderingFileLocator, FhirFileRegistry otherResources) {
		List<FormattedOutputSpec<?>> formatSpecs = Lists.newArrayList();
		
		Path outputDirectory = renderingFileLocator.getDestinationPathForResourceType(wrappedResource.getResourceType(), wrappedResource.getImplicitFhirVersion());
		
		if (wrappedResource instanceof WrappedConceptMap) {
			WrappedConceptMap wrappedConceptMap = (WrappedConceptMap)wrappedResource;
			formatSpecs.add(new FormattedOutputSpec<>(new ConceptMapFormatter(wrappedConceptMap, otherResources), outputDirectory, "full.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new ConceptMapMetadataFormatter(wrappedConceptMap, otherResources), outputDirectory, "metadata.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new ConceptMapTableFormatter(wrappedConceptMap, otherResources), outputDirectory, "mappings.html"));
		} else if (wrappedResource instanceof WrappedCodeSystem) {
			WrappedCodeSystem wrappedCodeSystem = (WrappedCodeSystem)wrappedResource;
			formatSpecs.add(new FormattedOutputSpec<>(new CodeSystemMetadataFormatter(wrappedCodeSystem, otherResources), outputDirectory, "metadata.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new CodeSystemFiltersTableFormatter(wrappedCodeSystem, otherResources), outputDirectory, "filters.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new CodeSystemConceptTableFormatter(wrappedCodeSystem, otherResources), outputDirectory, "concepts.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new CodeSystemFormatter(wrappedCodeSystem, otherResources), outputDirectory, "codesystem-full.html"));
		} else if (wrappedResource instanceof WrappedOperationDefinition) {
			WrappedOperationDefinition wrappedOperationDefinition = (WrappedOperationDefinition)wrappedResource;
			formatSpecs.add(new FormattedOutputSpec<>(new OperationDefinitionFormatter(wrappedOperationDefinition, otherResources), outputDirectory, "render.html"));
		} else if (wrappedResource instanceof WrappedValueSet) {
			WrappedValueSet wrappedValueSet = (WrappedValueSet)wrappedResource;
			formatSpecs.add(new FormattedOutputSpec<>(new ValueSetFormatter(wrappedValueSet, otherResources), outputDirectory, "render.html"));
		} else if (wrappedResource instanceof WrappedStructureDefinition) {
			WrappedStructureDefinition wrappedStructureDefinition = (WrappedStructureDefinition)wrappedResource;
			formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionMetadataFormatter(wrappedStructureDefinition, otherResources), outputDirectory, "metadata.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionSnapshotFormatter(wrappedStructureDefinition, otherResources), outputDirectory, "snapshot.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionBindingFormatter(wrappedStructureDefinition, otherResources), outputDirectory, "bindings.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionDetailsFormatter(wrappedStructureDefinition, otherResources), outputDirectory, "details.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionFormatter(wrappedStructureDefinition, otherResources), outputDirectory, "full.html"));
			
			if (!wrappedStructureDefinition.getConstrainedType().equals("Extension")) {
				formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionDifferentialFormatter(wrappedStructureDefinition, otherResources), outputDirectory, "differential.html"));
			}
		}
		else {
			throw new IllegalStateException("Unexpected wrapped resource class " + wrappedResource.getClass().getName());
		}
		
		
		
		return formatSpecs;
	}
}
