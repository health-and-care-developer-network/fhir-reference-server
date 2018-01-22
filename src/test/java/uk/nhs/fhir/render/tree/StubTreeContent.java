package uk.nhs.fhir.render.tree;

public class StubTreeContent extends TreeNode<Object, StubTreeContent> {

	private final String path;

	public StubTreeContent(String path) {
		this.path = path;
	}

	@Override
	public Object getData() {
		return null;
	}
	
	@Override
	public String getPath() {
		return path;
	}
	
	public boolean isDummy() {
		return false;
	}
}
