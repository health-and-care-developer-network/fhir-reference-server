package uk.nhs.fhir.servlet;

@SuppressWarnings("serial")
public class RequestIdMissingException extends RuntimeException {
	
	public RequestIdMissingException() {
		super("Could not parse qualified resource ID from request URL");
	}

}
