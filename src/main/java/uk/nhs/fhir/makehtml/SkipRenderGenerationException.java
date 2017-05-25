package uk.nhs.fhir.makehtml;

/**
 * intended for use in Non-strict mode only, to allow outstanding issues which cannot be worked around to be skipped. 
 */
@SuppressWarnings("serial")
public class SkipRenderGenerationException extends RuntimeException {

	public SkipRenderGenerationException(String message) {
		super(message);
	}
}
