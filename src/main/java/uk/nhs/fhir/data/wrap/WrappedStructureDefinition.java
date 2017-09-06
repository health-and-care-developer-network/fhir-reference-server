package uk.nhs.fhir.data.wrap;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.structdef.ExtensionType;
import uk.nhs.fhir.data.structdef.FhirContacts;
import uk.nhs.fhir.data.structdef.FhirMapping;
import uk.nhs.fhir.data.structdef.tree.FhirTreeData;
import uk.nhs.fhir.makehtml.FhirFileRegistry;
import uk.nhs.fhir.makehtml.FormattedOutputSpec;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;
import uk.nhs.fhir.makehtml.render.structdef.StructureDefinitionBindingFormatter;
import uk.nhs.fhir.makehtml.render.structdef.StructureDefinitionDetailsFormatter;
import uk.nhs.fhir.makehtml.render.structdef.StructureDefinitionDifferentialFormatter;
import uk.nhs.fhir.makehtml.render.structdef.StructureDefinitionFormatter;
import uk.nhs.fhir.makehtml.render.structdef.StructureDefinitionMetadataFormatter;
import uk.nhs.fhir.makehtml.render.structdef.StructureDefinitionSnapshotFormatter;

public abstract class WrappedStructureDefinition extends WrappedResource<WrappedStructureDefinition> {
	
	public abstract void checkUnimplementedFeatures();

	public abstract String getName();
	public abstract String getKind();
	public abstract String getStatus();
	public abstract Boolean getAbstract();

	public abstract Optional<String> getConstrainedType();
	public abstract String getBase();
	public abstract Optional<String> getVersion();
	public abstract Optional<String> getDisplay();
	public abstract Optional<String> getPublisher();
	public abstract Optional<Date> getDate();
	public abstract Optional<String> getCopyright();
	public abstract Optional<String> getFhirVersion();
	public abstract Optional<String> getContextType();
	public abstract Optional<String> getDescription();

	public abstract List<FhirContacts> getContacts();
	public abstract List<String> getUseContexts();
	public abstract List<FhirMapping> getMappings();
	public abstract List<String> getUseLocationContexts();

	public abstract FhirTreeData getSnapshotTree(FhirFileRegistry otherResources);
	public abstract FhirTreeData getDifferentialTree(FhirFileRegistry otherResources);
	
	public abstract ExtensionType getExtensionType();

	public abstract boolean missingSnapshot();
	
	protected abstract void setCopyright(String updatedCopyRight);
	
	@Override
	public ResourceFormatter<WrappedStructureDefinition> getDefaultViewFormatter(FhirFileRegistry otherResources) {
		return new StructureDefinitionFormatter(this, otherResources);
	}

	public boolean isExtension() {
		if (getConstrainedType().isPresent()) {
			return getConstrainedType().get().equals("Extension");
		} else {
			throw new IllegalStateException("Not sure whether this is an extension - no constrained type present");
		}
	}
	
	@Override
	public List<FormattedOutputSpec<WrappedStructureDefinition>> getFormatSpecs(String outputDirectory, FhirFileRegistry otherResources) {
		List<FormattedOutputSpec<WrappedStructureDefinition>> specs = Lists.newArrayList();

		specs.add(new FormattedOutputSpec<WrappedStructureDefinition>(this, new StructureDefinitionMetadataFormatter(this, otherResources), outputDirectory, "metadata.html"));
		specs.add(new FormattedOutputSpec<WrappedStructureDefinition>(this, new StructureDefinitionSnapshotFormatter(this, otherResources), outputDirectory, "snapshot.html"));
		specs.add(new FormattedOutputSpec<WrappedStructureDefinition>(this, new StructureDefinitionBindingFormatter(this, otherResources), outputDirectory, "bindings.html"));
		specs.add(new FormattedOutputSpec<WrappedStructureDefinition>(this, new StructureDefinitionDetailsFormatter(this, otherResources), outputDirectory, "details.html"));
		specs.add(new FormattedOutputSpec<WrappedStructureDefinition>(this, new StructureDefinitionFormatter(this, otherResources), outputDirectory, "full.html"));
		
		if (!getConstrainedType().equals("Extension")) {
			specs.add(
				new FormattedOutputSpec<WrappedStructureDefinition>(this, new StructureDefinitionDifferentialFormatter(this, otherResources), outputDirectory, "differential.html"));
		}
		
		return specs;
	}
	
	public String getOutputFolderName() {
		return "StructureDefinition";
	}
	
	public void fixHtmlEntities() {
		Optional<String> copyRight = getCopyright();
	    if(copyRight.isPresent()) {
	        String updatedCopyRight = copyRight.get().replace("©", "&#169;");
	        updatedCopyRight = updatedCopyRight.replace("\\u00a9", "&#169;");
	        setCopyright(updatedCopyRight);
	    }
	}
}
