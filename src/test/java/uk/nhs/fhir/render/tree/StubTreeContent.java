package uk.nhs.fhir.render.tree;

public class StubTreeContent extends TreeContent<StubTreeContent> {

	private final String path;

	public StubTreeContent(String path) {
		this.path = path;
	}
	
	@Override
	public String getPath() {
		return path;
	}
	
}
