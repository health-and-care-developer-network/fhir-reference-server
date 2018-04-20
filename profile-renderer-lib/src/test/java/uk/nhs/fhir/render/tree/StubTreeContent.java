package uk.nhs.fhir.render.tree;

import uk.nhs.fhir.data.structdef.tree.ImmutableNodePath;
import uk.nhs.fhir.data.structdef.tree.TreeNode;

public class StubTreeContent extends TreeNode<Object, StubTreeContent> {

	public StubTreeContent(String path) {
		super(new Object(), new ImmutableNodePath(path));
	}
	
	public boolean isDummy() {
		return false;
	}
}
