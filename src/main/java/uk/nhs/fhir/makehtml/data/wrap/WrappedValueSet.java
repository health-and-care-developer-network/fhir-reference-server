package uk.nhs.fhir.makehtml.data.wrap;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.FormattedOutputSpec;
import uk.nhs.fhir.makehtml.data.FhirCodeSystem;
import uk.nhs.fhir.makehtml.data.FhirValueSetCompose;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;
import uk.nhs.fhir.makehtml.render.valueset.ValueSetFormatter;

public abstract class WrappedValueSet extends WrappedResource<WrappedValueSet> {

	public abstract Optional<String> getCopyright();
	public abstract void setCopyright(String copyRight);
	public abstract List<WrappedConceptMap> getConceptMaps();
	public abstract Optional<String> getUrl();
	public abstract String getStatus();
	public abstract Optional<String> getOid();
	public abstract Optional<String> getReference();
	public abstract Optional<String> getVersion();
	public abstract Optional<String> getDescription();
	public abstract Optional<String> getPublisher();
	public abstract Optional<String> getRequirements();
	public abstract Optional<Date> getDate();
	public abstract boolean hasComposeIncludeFilter();
	public abstract FhirCodeSystem getCodeSystem();
	public abstract FhirValueSetCompose getCompose();
	
	@Override
	public ResourceFormatter<WrappedValueSet> getDefaultViewFormatter() {
		return new ValueSetFormatter(this);
	}

	@Override
	public List<FormattedOutputSpec<WrappedValueSet>> getFormatSpecs(String outputDirectory) {
		List<FormattedOutputSpec<WrappedValueSet>> specs = Lists.newArrayList();
		
		specs.add(new FormattedOutputSpec<WrappedValueSet>(this, new ValueSetFormatter(this), outputDirectory, "render.html"));
		
		return specs;
	}
	
	public String getOutputFolderName() {
		return "ValueSet";
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
