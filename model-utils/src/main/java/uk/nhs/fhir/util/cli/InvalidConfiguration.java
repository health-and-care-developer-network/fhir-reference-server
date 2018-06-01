package uk.nhs.fhir.util.cli;

@SuppressWarnings("serial")
public class InvalidConfiguration extends IllegalStateException {

	public InvalidConfiguration(String message) {
		super(message);
	}

}
