package uk.nhs.fhir.util.cli;

@SuppressWarnings("serial")
public class ArgParsingFailed extends IllegalStateException {

	public ArgParsingFailed(String message) {
		super(message);
	}

}
