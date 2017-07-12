package uk.nhs.fhir.makehtml.data.wrap;

import java.util.List;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.FormattedOutputSpec;
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
}
