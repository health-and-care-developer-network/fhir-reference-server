package uk.nhs.fhir.render;

public enum RendererExitStatus {
	FINISHED_OK(true),
	FINISHED_WITH_WARNINGS(true),
	FINISHED_WITH_ERRORS(false),
	CAUGHT_EXCEPTION(false);
	
	private final boolean success;
	
	RendererExitStatus(boolean success) {
		this.success = success;
	}
	
	public boolean succeeded() {
		return success;
	}
	
	public int exitCode() {
		return success ? 0 : 1;
	}
}
