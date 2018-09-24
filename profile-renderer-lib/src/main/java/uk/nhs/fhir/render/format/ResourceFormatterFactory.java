package uk.nhs.fhir.render.format;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.kohsuke.github.GHCommit;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.wrap.WrappedCodeSystem;
import uk.nhs.fhir.data.wrap.WrappedConceptMap;
import uk.nhs.fhir.data.wrap.WrappedMessageDefinition;
import uk.nhs.fhir.data.wrap.WrappedNamingSystem;
import uk.nhs.fhir.data.wrap.WrappedOperationDefinition;
import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.data.wrap.WrappedSearchParameter;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.data.wrap.WrappedValueSet;
import uk.nhs.fhir.render.FormattedOutputSpec;
import uk.nhs.fhir.render.GithubRepoDirectory;
import uk.nhs.fhir.render.RendererContext;
import uk.nhs.fhir.render.RendererFileLocator;
import uk.nhs.fhir.render.format.codesys.CodeSystemConceptTableFormatter;
import uk.nhs.fhir.render.format.codesys.CodeSystemFiltersTableFormatter;
import uk.nhs.fhir.render.format.codesys.CodeSystemFormatter;
import uk.nhs.fhir.render.format.codesys.CodeSystemMetadataFormatter;
import uk.nhs.fhir.render.format.conceptmap.ConceptMapFormatter;
import uk.nhs.fhir.render.format.conceptmap.ConceptMapMetadataFormatter;
import uk.nhs.fhir.render.format.conceptmap.ConceptMapTableFormatter;
import uk.nhs.fhir.render.format.githistory.GitHistoryFormatter;
import uk.nhs.fhir.render.format.message.MessageDefinitionFocusTableFormatter;
import uk.nhs.fhir.render.format.message.MessageDefinitionFormatter;
import uk.nhs.fhir.render.format.message.MessageDefinitionMetadataFormatter;
import uk.nhs.fhir.render.format.opdef.OperationDefinitionFormatter;
import uk.nhs.fhir.render.format.searchparam.SearchParameterFormatter;
import uk.nhs.fhir.render.format.searchparam.SearchParameterMetadataFormatter;
import uk.nhs.fhir.render.format.searchparam.SearchParameterTableFormatter;
import uk.nhs.fhir.render.format.structdef.StructureDefinitionBindingsTableFormatter;
import uk.nhs.fhir.render.format.structdef.StructureDefinitionDetailsFormatter;
import uk.nhs.fhir.render.format.structdef.StructureDefinitionDifferentialFormatter;
import uk.nhs.fhir.render.format.structdef.StructureDefinitionFormatter;
import uk.nhs.fhir.render.format.structdef.StructureDefinitionMetadataFormatter;
import uk.nhs.fhir.render.format.structdef.StructureDefinitionSnapshotFormatter;
import uk.nhs.fhir.render.format.valueset.ValueSetFormatter;
import uk.nhs.fhir.render.format.namingsystem.NamingSystemFormatter;

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
		} else if (wrappedResource instanceof WrappedMessageDefinition) { 
			return new MessageDefinitionFormatter((WrappedMessageDefinition) wrappedResource);
		} else if (wrappedResource instanceof WrappedSearchParameter) { 
			return new SearchParameterFormatter((WrappedSearchParameter) wrappedResource);
		} else if (wrappedResource instanceof WrappedNamingSystem) {  
			return new NamingSystemFormatter((WrappedNamingSystem) wrappedResource); 
		}
		else {
			throw new IllegalStateException("Unexpected wrapped resource class " + wrappedResource.getClass().getName());
		}
	}
	
	public <T extends WrappedResource<T>> List<FormattedOutputSpec<?>> allFormatterSpecs(
					T wrappedResource,
					RendererFileLocator rendererFileLocator,
					String filename) {
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
	
		} else if (wrappedResource instanceof WrappedNamingSystem) {
			WrappedNamingSystem wrappedNamingSystem = (WrappedNamingSystem)wrappedResource;
			formatSpecs.add(new FormattedOutputSpec<>(new NamingSystemFormatter(wrappedNamingSystem), outputDirectory, "render.html"));
		
		} else if (wrappedResource instanceof WrappedValueSet) {
			WrappedValueSet wrappedValueSet = (WrappedValueSet)wrappedResource;
			formatSpecs.add(new FormattedOutputSpec<>(new ValueSetFormatter(wrappedValueSet), outputDirectory, "render.html"));
		} else if (wrappedResource instanceof WrappedStructureDefinition) {
			WrappedStructureDefinition wrappedStructureDefinition = (WrappedStructureDefinition)wrappedResource;
			formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionMetadataFormatter(wrappedStructureDefinition), outputDirectory, "metadata.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionSnapshotFormatter(wrappedStructureDefinition), outputDirectory, "snapshot.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionBindingsTableFormatter(wrappedStructureDefinition), outputDirectory, "bindings.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionDetailsFormatter(wrappedStructureDefinition), outputDirectory, "details.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionFormatter(wrappedStructureDefinition), outputDirectory, "full.html"));
			if (!wrappedStructureDefinition.isExtension()) {
				formatSpecs.add(new FormattedOutputSpec<>(new StructureDefinitionDifferentialFormatter(wrappedStructureDefinition), outputDirectory, "differential.html"));
			}
		} else if (wrappedResource instanceof WrappedMessageDefinition) {
			WrappedMessageDefinition wrappedMessageDefinition = (WrappedMessageDefinition)wrappedResource;
			formatSpecs.add(new FormattedOutputSpec<>(new MessageDefinitionMetadataFormatter(wrappedMessageDefinition), outputDirectory, "metadata.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new MessageDefinitionFocusTableFormatter(wrappedMessageDefinition), outputDirectory, "focus.html"));
		} else if (wrappedResource instanceof WrappedSearchParameter) {
			WrappedSearchParameter wrappedSearchParameter = (WrappedSearchParameter)wrappedResource;
			formatSpecs.add(new FormattedOutputSpec<>(new SearchParameterMetadataFormatter(wrappedSearchParameter), outputDirectory, "metadata.html"));
			formatSpecs.add(new FormattedOutputSpec<>(new SearchParameterTableFormatter(wrappedSearchParameter), outputDirectory, "details.html"));
		} else {
			throw new IllegalStateException("Unexpected wrapped resource class " + wrappedResource.getClass().getName());
		}
		
		// Generate the Github history view for all resource types if Git details have been provided.
		if (RendererContext.forThread().github().getCurrentGitRepoDirectory().isPresent()) {
			
			GithubRepoDirectory gitRepoDirectory = RendererContext.forThread().github().getCurrentGitRepoDirectory().get();
			Optional<List<GHCommit>> artefactCommits = RendererContext.forThread().github().getGithubCommits(filename);
			
			if (artefactCommits.isPresent()) {
				ResourceFormatter<T> gitHistoryFormatter = new GitHistoryFormatter<>(wrappedResource, gitRepoDirectory.getBranch(), artefactCommits.get());
				formatSpecs.add(new FormattedOutputSpec<>(gitHistoryFormatter, outputDirectory, "git-history.html"));
			}
			
		}
		
		return formatSpecs;
	}
}
