package uk.nhs.fhir.util;

@SuppressWarnings("serial")
public class ResourceNotAvailableException extends IllegalStateException {

	public ResourceNotAvailableException(String message) {
		super(message);
	}

}
