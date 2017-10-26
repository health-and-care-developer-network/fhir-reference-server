package uk.nhs.fhir.server_renderer;

import uk.nhs.fhir.util.StringUtil;

public abstract class RendererOutputDisplay {
	public abstract void displayUpdate(String message);
	
	public void displayException(Exception e) {
		String stacktrace = StringUtil.getStackTrace(e);
		displayUpdate(stacktrace);
	}
}
