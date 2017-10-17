package uk.nhs.fhir.makehtml;

import java.io.IOException;

@SuppressWarnings("serial")
public class FhirParsingFailedException extends Exception {

	public FhirParsingFailedException(String message) {
		super(message);
	}

	public FhirParsingFailedException(String message, IOException e) {
		super(message, e);
	}

}
