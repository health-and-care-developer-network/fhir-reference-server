package uk.nhs.fhir.server_renderer;

public class ConsolerRendererOutputDisplay extends RendererOutputDisplay {

	@Override
	public void displayUpdate(String message) {
		System.out.println("Renderer ouput: " + message);
	}

}
