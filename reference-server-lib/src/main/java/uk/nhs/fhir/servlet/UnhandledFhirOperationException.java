package uk.nhs.fhir.servlet;

@SuppressWarnings("serial")
public class UnhandledFhirOperationException extends RuntimeException {
	
	public UnhandledFhirOperationException(String operation) {
		super("Unsupported operation: " + operation);
	}
}
