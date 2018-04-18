package uk.nhs.fhir.render;

@SuppressWarnings("serial")
public class InvalidConfiguration extends IllegalStateException {

	public InvalidConfiguration(String message) {
		super(message);
	}

}
