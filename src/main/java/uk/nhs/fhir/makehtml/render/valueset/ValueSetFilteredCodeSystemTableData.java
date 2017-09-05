package uk.nhs.fhir.makehtml.render.valueset;

import uk.nhs.fhir.data.url.FhirURL;

public class ValueSetFilteredCodeSystemTableData {
	private final FhirURL codeSystem;
	private final String concept;
	private final String operation;
	private final String value;
	
	public ValueSetFilteredCodeSystemTableData(FhirURL codeSystem, String concept, String operation, String value) {
		this.codeSystem = codeSystem;
		this.concept = concept;
		this.operation = operation;
		this.value = value;
	}
	
	public FhirURL getCodeSystem() {
		return codeSystem;
	}
	
	public String getConcept() {
		return concept;
	}
	
	public String getOperation() {
		return operation;
	}
	
	public String getValue() {
		return value;
	}
}
