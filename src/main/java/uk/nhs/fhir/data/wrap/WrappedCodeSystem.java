package uk.nhs.fhir.data.wrap;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.codesystem.FhirCodeSystem;
import uk.nhs.fhir.data.codesystem.FhirCodeSystemFilter;
import uk.nhs.fhir.data.codesystem.FhirIdentifier;
import uk.nhs.fhir.data.structdef.FhirContacts;
import uk.nhs.fhir.makehtml.FormattedOutputSpec;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;
import uk.nhs.fhir.makehtml.render.codesys.CodeSystemFiltersTableFormatter;
import uk.nhs.fhir.makehtml.render.codesys.CodeSystemMetadataFormatter;

public abstract class WrappedCodeSystem extends WrappedResource<WrappedCodeSystem> {
	public abstract String getName();
	public abstract Optional<String> getTitle();
	public abstract String getUrl();
	public abstract String getStatus();

	public abstract Optional<String> getVersion();
	public abstract Optional<String> getDisplay();
	public abstract Optional<String> getPublisher();
	public abstract Optional<Date> getLastUpdatedDate();
	public abstract Optional<String> getCopyright();
	public abstract Optional<String> getValueSet();
	
	public abstract Optional<FhirIdentifier> getIdentifier();
	
	public abstract Optional<Boolean> getExperimental();
	public abstract Optional<String> getDescription();
	public abstract Optional<String> getPurpose();
	public abstract Optional<Boolean> getCaseSensitive();
	public abstract Optional<Boolean> getCompositional();
	
	public abstract Optional<String> getContent();
	public abstract Optional<String> getHierarchyMeaning();
	public abstract List<FhirContacts> getContacts();
	
	public abstract FhirCodeSystem getCodeSystem();
	public abstract List<FhirCodeSystemFilter> getFilters();

	@Override
	public String getOutputFolderName() {
		return "CodeSystem";
	}

	@Override
	public ResourceFormatter<WrappedCodeSystem> getDefaultViewFormatter() {
		return new CodeSystemMetadataFormatter(this);
	}

	@Override
	public List<FormattedOutputSpec<WrappedCodeSystem>> getFormatSpecs(String outputDirectory) {
		List<FormattedOutputSpec<WrappedCodeSystem>> formatSpecs = Lists.newArrayList();

		formatSpecs.add(new FormattedOutputSpec<>(this, new CodeSystemMetadataFormatter(this), outputDirectory, "metadata.html"));
		formatSpecs.add(new FormattedOutputSpec<>(this, new CodeSystemFiltersTableFormatter(this), outputDirectory, "filters.html"));
		
		return formatSpecs;
	}
}
