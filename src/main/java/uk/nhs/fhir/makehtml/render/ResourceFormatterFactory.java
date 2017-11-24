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
import uk.nhs.fhir.makehtml.FormattedOutputSpec;
import uk.nhs.fhir.makehtml.RendererFileLocator;
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

public class ResourceFormatterFactory {
	public ResourceFormatter<?> defaultFormatter(WrappedResource<?> wrappedResource, RendererContext context) {
		if (wrappedResource instanceof WrappedConceptMap) {
			return new ConceptMapFormatter((WrappedConceptMap) wrappedResource, context);
		} else if (wrappedResource instanceof WrappedCodeSystem) {
			return new CodeSystemFormatter((WrappedCodeSystem) wrappedResource, context);
		} else if (wrappedResource instanceof WrappedOperationDefinition) {
			return new OperationDefinitionFormatter((WrappedOperationDefinition) wrappedResource, context);
		} else if (wrappedResource instanceof WrappedValueSet) {
			return new ValueSetFormatter((WrappedValueSet) wrappedResource, context);
		} else if (wrappedResource instanceof WrappedStructureDefinition) {
			return new StructureDefinitionFormatter((WrappedStructureDefinition) wrappedResource, context);
		} else {
			throw new IllegalStateException("Unexpected wrapped resource class " + wrappedResource.getClass().getName());
		}
	}
	
	public List<FormattedOutputSpec<?>> allFormatterSpecs(WrappedResource<?> wrappedResource, RendererFileLocator rendererFileLocator, RendererContext context) {
		List<FormattedOutputSpec<?>> formatSpecs = Lists.newArrayList();
		
		Path outputDirectory = rendererFileLocator.getRenderingTempOutputDirectory(wrappedResource);
		
		if (wrappedResource instanceof WrappedConceptMap) {
			WrappedConceptMap wrappedConceptMap = (WrappedConceptMap)wrappedResource;
			formatSpecs.add(new FormattedOutputSpec<>(new ConceptMapFormatter(wrappedConceptMap, context), outputDirectory, "full.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new ConceptMapMetadataFormatter(wrappedConceptMap, context), outputDirectory, "metadata.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new ConceptMapTableFormatter(wrappedConceptMap, context), outputDirectory, "mappings.html"));
		} else if (wrappedResource instanceof WrappedCodeSystem) {
			WrappedCodeSystem wrappedCodeSystem = (WrappedCodeSystem)wrappedResource;
			formatSpecs.add(new FormattedOutputSpec<>(new CodeSystemMetadataFormatter(wrappedCodeSystem, context), outputDirectory, "metadata.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new CodeSystemFiltersTableFormatter(wrappedCodeSystem, context), outputDirectory, "filters.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new CodeSystemConceptTableFormatter(wrappedCodeSystem, context), outputDirectory, "concepts.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new CodeSystemFormatter(wrappedCodeSystem, context), outputDirectory, "codesystem-full.html"));
		} else if (wrappedResource instanceof WrappedOperationDefinition) {
			WrappedOperationDefinition wrappedOperationDefinition = (WrappedOperationDefinition)wrappedResource;
			formatSpecs.add(new FormattedOutputSpec<>(new OperationDefinitionFormatter(wrappedOperationDefinition, context), outputDirectory, "render.html"));
		} else if (wrappedResource instanceof WrappedValueSet) {
			WrappedValueSet wrappedValueSet = (WrappedValueSet)wrappedResource;
			formatSpecs.add(new FormattedOutputSpec<>(new ValueSetFormatter(wrappedValueSet, context), outputDirectory, "render.html"));
		} else if (wrappedResource instanceof WrappedStructureDefinition) {
			WrappedStructureDefinition wrappedStructureDefinition = (WrappedStructureDefinition)wrappedResource;
			formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionMetadataFormatter(wrappedStructureDefinition, context), outputDirectory, "metadata.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionSnapshotFormatter(wrappedStructureDefinition, context), outputDirectory, "snapshot.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionBindingFormatter(wrappedStructureDefinition, context), outputDirectory, "bindings.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionDetailsFormatter(wrappedStructureDefinition, context), outputDirectory, "details.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionFormatter(wrappedStructureDefinition, context), outputDirectory, "full.html"));
			
			if (!wrappedStructureDefinition.getConstrainedType().equals("Extension")) {
				formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionDifferentialFormatter(wrappedStructureDefinition, context), outputDirectory, "differential.html"));
			}
		}
		else {
			throw new IllegalStateException("Unexpected wrapped resource class " + wrappedResource.getClass().getName());
		}
		
		
		
		return formatSpecs;
	}
}
