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
	public ResourceFormatter<?> defaultFormatter(WrappedResource<?> wrappedResource) {
		if (wrappedResource instanceof WrappedConceptMap) {
			return new ConceptMapFormatter((WrappedConceptMap) wrappedResource);
		} else if (wrappedResource instanceof WrappedCodeSystem) {
			return new CodeSystemFormatter((WrappedCodeSystem) wrappedResource);
		} else if (wrappedResource instanceof WrappedOperationDefinition) {
			return new OperationDefinitionFormatter((WrappedOperationDefinition) wrappedResource);
		} else if (wrappedResource instanceof WrappedValueSet) {
			return new ValueSetFormatter((WrappedValueSet) wrappedResource);
		} else if (wrappedResource instanceof WrappedStructureDefinition) {
			return new StructureDefinitionFormatter((WrappedStructureDefinition) wrappedResource);
		} else {
			throw new IllegalStateException("Unexpected wrapped resource class " + wrappedResource.getClass().getName());
		}
	}
	
	public List<FormattedOutputSpec<?>> allFormatterSpecs(WrappedResource<?> wrappedResource, RendererFileLocator rendererFileLocator) {
		List<FormattedOutputSpec<?>> formatSpecs = Lists.newArrayList();
		
		Path outputDirectory = rendererFileLocator.getRenderingTempOutputDirectory(wrappedResource);
		
		if (wrappedResource instanceof WrappedConceptMap) {
			WrappedConceptMap wrappedConceptMap = (WrappedConceptMap)wrappedResource;
			formatSpecs.add(new FormattedOutputSpec<>(new ConceptMapFormatter(wrappedConceptMap), outputDirectory, "full.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new ConceptMapMetadataFormatter(wrappedConceptMap), outputDirectory, "metadata.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new ConceptMapTableFormatter(wrappedConceptMap), outputDirectory, "mappings.html"));
		} else if (wrappedResource instanceof WrappedCodeSystem) {
			WrappedCodeSystem wrappedCodeSystem = (WrappedCodeSystem)wrappedResource;
			formatSpecs.add(new FormattedOutputSpec<>(new CodeSystemMetadataFormatter(wrappedCodeSystem), outputDirectory, "metadata.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new CodeSystemFiltersTableFormatter(wrappedCodeSystem), outputDirectory, "filters.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new CodeSystemConceptTableFormatter(wrappedCodeSystem), outputDirectory, "concepts.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new CodeSystemFormatter(wrappedCodeSystem), outputDirectory, "codesystem-full.html"));
		} else if (wrappedResource instanceof WrappedOperationDefinition) {
			WrappedOperationDefinition wrappedOperationDefinition = (WrappedOperationDefinition)wrappedResource;
			formatSpecs.add(new FormattedOutputSpec<>(new OperationDefinitionFormatter(wrappedOperationDefinition), outputDirectory, "render.html"));
		} else if (wrappedResource instanceof WrappedValueSet) {
			WrappedValueSet wrappedValueSet = (WrappedValueSet)wrappedResource;
			formatSpecs.add(new FormattedOutputSpec<>(new ValueSetFormatter(wrappedValueSet), outputDirectory, "render.html"));
		} else if (wrappedResource instanceof WrappedStructureDefinition) {
			WrappedStructureDefinition wrappedStructureDefinition = (WrappedStructureDefinition)wrappedResource;
			formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionMetadataFormatter(wrappedStructureDefinition), outputDirectory, "metadata.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionSnapshotFormatter(wrappedStructureDefinition), outputDirectory, "snapshot.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionBindingFormatter(wrappedStructureDefinition), outputDirectory, "bindings.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionDetailsFormatter(wrappedStructureDefinition), outputDirectory, "details.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionFormatter(wrappedStructureDefinition), outputDirectory, "full.html"));
			
			if (!wrappedStructureDefinition.getConstrainedType().equals("Extension")) {
				formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionDifferentialFormatter(wrappedStructureDefinition), outputDirectory, "differential.html"));
			}
		}
		else {
			throw new IllegalStateException("Unexpected wrapped resource class " + wrappedResource.getClass().getName());
		}
		
		
		
		return formatSpecs;
	}
}
