package uk.nhs.fhir.makehtml.render.codesys;

import java.util.Optional;

public class CodeSystemFilterTableRowData {

	private final String code;
	private final String operators;
	private final String value;
	private final Optional<String> documentation;

	public CodeSystemFilterTableRowData(String code, String operators, String value, Optional<String> documentation) {
		this.code = code;
		this.operators = operators;
		this.value = value;
		this.documentation = documentation;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getOperators() {
		return operators;
	}
	
	public String getValue() {
		return value;
	}
	
	public boolean hasDocumentation() {
		return documentation.isPresent();
	}
	
	public Optional<String> getDocumentation() {
		return documentation;
	}

}
