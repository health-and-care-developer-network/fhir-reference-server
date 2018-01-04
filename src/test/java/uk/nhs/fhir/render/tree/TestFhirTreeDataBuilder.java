package uk.nhs.fhir.render.tree;

import org.junit.Test;

public class TestFhirTreeDataBuilder {
	@Test
	public void testSingleNodeTree() {
		StubTreeContent root = new StubTreeContent("test");
		SnapshotTreeDataBuilder<StubTreeContent> treeBuilder = new SnapshotTreeDataBuilder(null);
		treeBuilder.addFhirTreeNode(root);
		
	}
}
