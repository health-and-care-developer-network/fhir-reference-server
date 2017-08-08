package uk.nhs.fhir.makehtml.data;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

public class FhirValueSetComposeInclude {

	private final String system;
	private final Optional<String> version;
	
	private final List<FhirValueSetComposeIncludeFilter> filters = Lists.newArrayList();
	private final List<FhirValueSetComposeIncludeConcept> concepts = Lists.newArrayList();
	
	public FhirValueSetComposeInclude(String system, String version) {
		this.system = system;
		this.version = Optional.ofNullable(version);
	}

	public String getSystem() {
		return system;
	}

	public Optional<String> getVersion() {
		return version;
	}

	public void addFilter(FhirValueSetComposeIncludeFilter filter) {
		filters.add(filter);
	}
	
	public List<FhirValueSetComposeIncludeFilter> getFilters() {
		return filters;
	}

	public void addConcept(FhirValueSetComposeIncludeConcept concept) {
		concepts.add(concept);
	}

	public List<FhirValueSetComposeIncludeConcept> getConcepts() {
		return concepts ;
	}

}
