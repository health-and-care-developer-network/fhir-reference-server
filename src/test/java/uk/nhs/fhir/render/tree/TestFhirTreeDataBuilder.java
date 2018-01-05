package uk.nhs.fhir.render.tree;

import org.junit.Test;

import junit.framework.Assert;

public class TestFhirTreeDataBuilder {
	@Test
	public void testSingleNodeTree() {
		StubTreeContent root = new StubTreeContent("test");
		FhirTreeDataBuilder<StubTreeContent> treeBuilder = new FhirTreeDataBuilder<>();
		treeBuilder.addFhirTreeNode(root);
		
		FhirTreeData<StubTreeContent> tree = treeBuilder.getTree();
		
		Assert.assertEquals(root, tree.getRoot());
	}
}
