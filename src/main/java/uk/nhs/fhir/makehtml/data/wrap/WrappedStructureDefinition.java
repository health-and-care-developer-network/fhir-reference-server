package uk.nhs.fhir.makehtml.data.wrap;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.FormattedOutputSpec;
import uk.nhs.fhir.makehtml.data.FhirContact;
import uk.nhs.fhir.makehtml.data.FhirMapping;
import uk.nhs.fhir.makehtml.data.FhirTreeData;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;
import uk.nhs.fhir.makehtml.render.structdef.StructureDefinitionBindingFormatter;
import uk.nhs.fhir.makehtml.render.structdef.StructureDefinitionDetailsFormatter;
import uk.nhs.fhir.makehtml.render.structdef.StructureDefinitionDifferentialFormatter;
import uk.nhs.fhir.makehtml.render.structdef.StructureDefinitionMetadataFormatter;
import uk.nhs.fhir.makehtml.render.structdef.StructureDefinitionSnapshotFormatter;

public abstract class WrappedStructureDefinition extends WrappedResource<WrappedStructureDefinition> {
	
	public abstract boolean isExtension();
	
	public abstract void checkUnimplementedFeatures();

	public abstract String getName();
	public abstract String getUrl();
	public abstract String getKind();
	public abstract String getStatus();
	public abstract Boolean getAbstract();

	public abstract Optional<String> getConstrainedType();
	public abstract String getBase();
	public abstract Optional<String> getVersion();
	public abstract Optional<String> getDisplay();
	public abstract Optional<String> getPublisher();
	public abstract Date getDate();
	public abstract Optional<String> getCopyright();
	public abstract Optional<String> getFhirVersion();
	public abstract Optional<String> getContextType();

	public abstract List<FhirContact> getContacts();
	public abstract List<String> getUseContexts();
	public abstract List<FhirMapping> getMappings();
	public abstract List<String> getUseLocationContexts();

	public abstract FhirTreeData getSnapshotTree();
	public abstract FhirTreeData getDifferentialTree();
	
	@Override
	public ResourceFormatter<WrappedStructureDefinition> getDefaultViewFormatter() {
		return new StructureDefinitionSnapshotFormatter();
	}

	@Override
	public List<FormattedOutputSpec<WrappedStructureDefinition>> getFormatSpecs(String outputDirectory) {
		List<FormattedOutputSpec<WrappedStructureDefinition>> specs = Lists.newArrayList();

		specs.add(new FormattedOutputSpec<WrappedStructureDefinition>(this, new StructureDefinitionMetadataFormatter(), outputDirectory, "metadata.html"));
		specs.add(new FormattedOutputSpec<WrappedStructureDefinition>(this, new StructureDefinitionSnapshotFormatter(), outputDirectory, "snapshot.html"));
		specs.add(new FormattedOutputSpec<WrappedStructureDefinition>(this, new StructureDefinitionBindingFormatter(), outputDirectory, "bindings.html"));
		specs.add(new FormattedOutputSpec<WrappedStructureDefinition>(this, new StructureDefinitionDetailsFormatter(), outputDirectory, "details.html"));
		
		if (!getConstrainedType().equals("Extension")) {
			specs.add(
				new FormattedOutputSpec<WrappedStructureDefinition>(this, new StructureDefinitionDifferentialFormatter(), outputDirectory, "differential.html"));
		}
		
		return specs;
	}
}
