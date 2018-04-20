package uk.nhs.fhir.data.valueset;

import java.util.List;

import com.google.common.collect.Lists;

public class FhirValueSetCompose {
	private final List<String> importUris = Lists.newArrayList();
	private final List<FhirValueSetComposeInclude> includes = Lists.newArrayList();
	private final List<FhirValueSetComposeInclude> excludes = Lists.newArrayList();
	
	public void addImportUri(String importUri) {
		importUris.add(importUri);
	}
	
	public List<String> getImportUris() {
		return importUris;
	}
	
	public void addInclude(FhirValueSetComposeInclude include) {
		includes.add(include);
	}
	
	public List<FhirValueSetComposeInclude> getIncludes() {
		return includes;
	}
	
	public void addExclude(FhirValueSetComposeInclude exclude) {
		excludes.add(exclude);
	}
	
	public List<FhirValueSetComposeInclude> getExcludes() {
		return excludes;
	}
}
