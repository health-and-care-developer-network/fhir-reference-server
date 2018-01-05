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
		Assert.assertEquals(0, root.getChildren().size());
	}
	
	@Test
	public void testRootAndChildTree() {
		StubTreeContent rootNode = new StubTreeContent("root");
		FhirTreeDataBuilder<StubTreeContent> treeBuilder = new FhirTreeDataBuilder<>();
		treeBuilder.addFhirTreeNode(rootNode);
		StubTreeContent child = new StubTreeContent("root.child");
		treeBuilder.addFhirTreeNode(child);
		
		FhirTreeData<StubTreeContent> tree = treeBuilder.getTree();
		
		StubTreeContent root = tree.getRoot();
		Assert.assertEquals(rootNode, root);
		Assert.assertEquals(1, root.getChildren().size());
		Assert.assertEquals(child, root.getChildren().get(0));
		Assert.assertEquals(0, root.getChildren().get(0).getChildren().size());
	}
	
	@Test
	public void testRootAndChildAndGrandChildTree() {
		StubTreeContent rootNode = new StubTreeContent("root");
		FhirTreeDataBuilder<StubTreeContent> treeBuilder = new FhirTreeDataBuilder<>();
		treeBuilder.addFhirTreeNode(rootNode);
		StubTreeContent childNode = new StubTreeContent("root.child");
		treeBuilder.addFhirTreeNode(childNode);
		StubTreeContent grandchildNode = new StubTreeContent("root.child.grandchild");
		treeBuilder.addFhirTreeNode(grandchildNode);
		
		FhirTreeData<StubTreeContent> tree = treeBuilder.getTree();
		
		StubTreeContent root = tree.getRoot();
		Assert.assertEquals(rootNode, root);
		Assert.assertEquals(1, root.getChildren().size());
		
		StubTreeContent child = root.getChildren().get(0);
		Assert.assertEquals(childNode, child);
		Assert.assertEquals(1, child.getChildren().size());
		
		StubTreeContent grandchild = child.getChildren().get(0);
		Assert.assertEquals(grandchildNode, grandchild);
		Assert.assertEquals(0, grandchild.getChildren().size());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testFailToMakeDummyIfNoDummyFactoryProvided() {
		FhirTreeDataBuilder<StubTreeContent> treeBuilder = new FhirTreeDataBuilder<>();
		
		StubTreeContent rootNode = new StubTreeContent("root");
		treeBuilder.addFhirTreeNode(rootNode);
		StubTreeContent grandchildNode = new StubTreeContent("root.child.grandchild");
		treeBuilder.addFhirTreeNode(grandchildNode);
	}
	
	@Test
	public void testMakeDummy() {
		FhirTreeDataBuilder<StubTreeContent> treeBuilder = new FhirTreeDataBuilder<>(new DummyStubTreeContentFactory());
		
		StubTreeContent rootNode = new StubTreeContent("root");
		treeBuilder.addFhirTreeNode(rootNode);
		StubTreeContent grandchildNode = new StubTreeContent("root.child.grandchild");
		treeBuilder.addFhirTreeNode(grandchildNode);
		
		FhirTreeData<StubTreeContent> tree = treeBuilder.getTree();
		
		StubTreeContent root = tree.getRoot();
		Assert.assertEquals(rootNode, root);
		Assert.assertEquals(1, root.getChildren().size());
		
		// inserted automatically, since we have a dummy node factory available
		StubTreeContent child = root.getChildren().get(0);
		Assert.assertTrue(child.isDummy());
		Assert.assertEquals(1, child.getChildren().size());
		
		StubTreeContent grandchild = child.getChildren().get(0);
		Assert.assertEquals(grandchildNode, grandchild);
		Assert.assertEquals(0, grandchild.getChildren().size());
	}
}

class DummyStubTreeContent extends StubTreeContent {
	public DummyStubTreeContent(String path) {
		super(path);
	}
	
	@Override
	public boolean isDummy() {
		return true;
	}
}

class DummyStubTreeContentFactory implements DummyNodeFactory<StubTreeContent> {

	@Override
	public StubTreeContent create(StubTreeContent parent, NodePath path) {
		return new DummyStubTreeContent(path.toPathString());
	}
	
}
