package uk.nhs.fhir.data.wrap;

import java.util.List;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.opdef.FhirOperationParameter;
import uk.nhs.fhir.data.url.LinkData;
import uk.nhs.fhir.makehtml.FhirFileRegistry;
import uk.nhs.fhir.makehtml.FormattedOutputSpec;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;
import uk.nhs.fhir.makehtml.render.opdef.OperationDefinitionFormatter;

public abstract class WrappedOperationDefinition extends WrappedResource<WrappedOperationDefinition> {

	@Override
	public ResourceFormatter<WrappedOperationDefinition> getDefaultViewFormatter(FhirFileRegistry otherResources) {
		return new OperationDefinitionFormatter(this, otherResources);
	}

	@Override
	public List<FormattedOutputSpec<WrappedOperationDefinition>> getFormatSpecs(String outputDirectory, FhirFileRegistry otherResources) {
		List<FormattedOutputSpec<WrappedOperationDefinition>> specs = Lists.newArrayList();
		specs.add(new FormattedOutputSpec<WrappedOperationDefinition>(this, new OperationDefinitionFormatter(this, otherResources), outputDirectory, "render.html"));
		return specs;
	}
	
	public String getOutputFolderName() {
		return "OperationDefinition";
	}

	public abstract String getName();
	public abstract LinkData getNameTypeLink();
	public abstract String getKind();
	public abstract LinkData getKindTypeLink();
	public abstract String getDescription();
	public abstract LinkData getDescriptionTypeLink();
	public abstract String getCode();
	public abstract LinkData getCodeTypeLink();
	public abstract LinkData getSystemTypeLink();
	public abstract String getIsSystem();
	public abstract LinkData getInstanceTypeLink();
	public abstract String getIsInstance();

	public abstract List<FhirOperationParameter> getInputParameters();
	public abstract List<FhirOperationParameter> getOutputParameters();
}
