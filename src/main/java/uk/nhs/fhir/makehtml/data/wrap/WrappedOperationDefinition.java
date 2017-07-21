package uk.nhs.fhir.makehtml.data.wrap;

import java.util.List;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.FormattedOutputSpec;
import uk.nhs.fhir.makehtml.data.FhirOperationParameter;
import uk.nhs.fhir.makehtml.data.LinkData;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;
import uk.nhs.fhir.makehtml.render.opdef.OperationDefinitionFormatter;

public abstract class WrappedOperationDefinition extends WrappedResource<WrappedOperationDefinition> {

	@Override
	public ResourceFormatter<WrappedOperationDefinition> getDefaultViewFormatter() {
		return new OperationDefinitionFormatter();
	}

	@Override
	public List<FormattedOutputSpec<WrappedOperationDefinition>> getFormatSpecs(String outputDirectory) {
		List<FormattedOutputSpec<WrappedOperationDefinition>> specs = Lists.newArrayList();
		specs.add(new FormattedOutputSpec<WrappedOperationDefinition>(this, new OperationDefinitionFormatter(), outputDirectory, "render.html"));
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
