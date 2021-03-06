package uk.nhs.fhir.data.valueset;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.codesystem.FhirCodeSystemConcept;

public class FhirValueSetComposeInclude {

	private final String system;
	private final Optional<String> version;
	
	private final List<FhirValueSetComposeIncludeFilter> filters = Lists.newArrayList();
	private final List<FhirCodeSystemConcept> concepts = Lists.newArrayList();
	
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

	public void addConcept(FhirCodeSystemConcept concept) {
		concepts.add(concept);
	}

	public List<FhirCodeSystemConcept> getConcepts() {
		return concepts ;
	}

}
