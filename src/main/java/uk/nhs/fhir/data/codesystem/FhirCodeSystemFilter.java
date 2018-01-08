package uk.nhs.fhir.data.codesystem;

import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class FhirCodeSystemFilter {
	private final String code;
	private final List<String> permittedOperators = Lists.newArrayList();
	private final String value;
	private final Optional<String> documentation;
	
	
	public FhirCodeSystemFilter(String code, String documentation, String value) {
		this.code = Preconditions.checkNotNull(code);
		this.value = Preconditions.checkNotNull(value);
		this.documentation = Optional.ofNullable(documentation);
	}
	
	public String getCode() {
		return code;
	}
	
	public void addOperator(String operator) {
		permittedOperators.add(operator);
	}
	
	public List<String> getPermittedOperators() {
		return permittedOperators;
	}
	
	public String getValue() {
		return value;
	}
	
	public Optional<String> getDocumentation() {
		return documentation;
	}
}
