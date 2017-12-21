package uk.nhs.fhir.makehtml;

@SuppressWarnings("serial")
public class LoggedRenderingException extends IllegalStateException {

	public LoggedRenderingException() {}

	public LoggedRenderingException(String s) {
		super(s);
	}

	public LoggedRenderingException(Exception e) {
		super(e);
	}

	public LoggedRenderingException(String s, Exception e) {
		super(s, e);
	}
	
}
