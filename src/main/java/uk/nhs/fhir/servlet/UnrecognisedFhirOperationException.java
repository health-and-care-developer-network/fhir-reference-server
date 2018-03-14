package uk.nhs.fhir.servlet;

@SuppressWarnings("serial")
public class UnrecognisedFhirOperationException extends RuntimeException {
	public UnrecognisedFhirOperationException(String operation) {
		super("\"" + operation + "\" is not a recognised FHIR operation");
	}
}
