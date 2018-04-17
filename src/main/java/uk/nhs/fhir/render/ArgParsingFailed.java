package uk.nhs.fhir.render;

@SuppressWarnings("serial")
public class ArgParsingFailed extends IllegalStateException {

	public ArgParsingFailed(String message) {
		super(message);
	}

}
