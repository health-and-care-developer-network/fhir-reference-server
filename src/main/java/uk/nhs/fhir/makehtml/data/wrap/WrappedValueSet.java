package uk.nhs.fhir.makehtml.data.wrap;

import java.util.List;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.FormattedOutputSpec;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;
import uk.nhs.fhir.makehtml.render.valueset.ValueSetFormatter;

public abstract class WrappedValueSet extends WrappedResource<WrappedValueSet> {
	
	@Override
	public ResourceFormatter<WrappedValueSet> getDefaultViewFormatter() {
		return new ValueSetFormatter();
	}


	@Override
	public List<FormattedOutputSpec<WrappedValueSet>> getFormatSpecs(String outputDirectory) {
		List<FormattedOutputSpec<WrappedValueSet>> specs = Lists.newArrayList();
		
		specs.add(new FormattedOutputSpec<WrappedValueSet>(this, new ValueSetFormatter(), outputDirectory, "render.html"));
		
		return specs;
	}

}
