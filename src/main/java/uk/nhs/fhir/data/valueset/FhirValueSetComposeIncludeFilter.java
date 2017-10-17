package uk.nhs.fhir.data.valueset;

public class FhirValueSetComposeIncludeFilter {

	private final String property;
	private final String op;
	private final String value;
	
	public FhirValueSetComposeIncludeFilter(String property, String op, String value) {
		this.property = property;
		this.op = op;
		this.value = value;
	}

	public String getProperty() {
		return property;
	}

	public String getOp() {
		return op;
	}

	public String getValue() {
		return value;
	}

}
