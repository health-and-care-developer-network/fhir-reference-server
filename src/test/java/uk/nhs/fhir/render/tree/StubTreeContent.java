package uk.nhs.fhir.render.tree;

import uk.nhs.fhir.data.structdef.tree.TreeNode;

public class StubTreeContent extends TreeNode<Object, StubTreeContent> {

	private final String path;

	public StubTreeContent(String path) {
		super(new Object());
		this.path = path;
	}
	
	@Override
	public String getPath() {
		return path;
	}
	
	public boolean isDummy() {
		return false;
	}
}
