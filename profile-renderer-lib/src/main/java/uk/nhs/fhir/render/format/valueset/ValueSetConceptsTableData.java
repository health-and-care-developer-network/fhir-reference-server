package uk.nhs.fhir.render.format.valueset;

import java.util.Optional;

public class ValueSetConceptsTableData {
	private final String code;
	private final Optional<String> display;
	private final Optional<String> definition;
	private final Optional<String> mapping;
	
	public ValueSetConceptsTableData(String code, Optional<String> display, Optional<String> definition, Optional<String> mapping) {
		this.code = code;
		this.display = display;
		this.definition = definition;
		this.mapping = mapping;
	}
	
	public String getCode() {
		return code;
	}
	
	public Optional<String> getDisplay() {
		return display;
	}
	
	public Optional<String> getDefinition() {
		return definition;
	}
	
	public Optional<String> getMapping() {
		return mapping;
	}
}
