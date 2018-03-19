package uk.nhs.fhir.render.data;

import org.junit.Assert;

import org.junit.Test;

import com.google.common.collect.Iterators;

import uk.nhs.fhir.data.structdef.tree.FhirTreeData;
import uk.nhs.fhir.data.structdef.tree.ImmutableNodePath;
import uk.nhs.fhir.data.structdef.tree.TreeNode;

public class TestFhirTreeData {
	
	public static int nodesCount(FhirTreeData<Object, TestFhirTreeNode> data) {
		Iterators.size(data.iterator());
		
		int total = 0;
		
		for (TestFhirTreeNode node : data.nodes()) {
			Assert.assertNotNull(node);
			total++;
		}
		
		return total;
	}
	
	/*
	 * ROOT
	 */
	@Test
	public void testIterateSingleNode() {
		TestFhirTreeNode root = new TestFhirTreeNode("ROOT", "Test");
		FhirTreeData<Object, TestFhirTreeNode> data = new FhirTreeData<>(root);
		
		Assert.assertEquals(1, Iterators.size(data.iterator()));
	}

	/*
	 * ROOT
	 *  |---|
	 *    CHILD
	 */
	@Test
	public void testIterateNodeWithChild() {
		TestFhirTreeNode root = new TestFhirTreeNode("ROOT", "Test");
		root.addChild(new TestFhirTreeNode("CHILD", "Test.child"));
		FhirTreeData<Object, TestFhirTreeNode> data = new FhirTreeData<>(root);

		Assert.assertEquals(2, Iterators.size(data.iterator()));
	}

	/*
	 * ROOT
	 *  |
	 *  |---CHILD
	 *        |
	 *        |---GRANDCHILD
	 */	
	@Test
	public void testIterateNodeWithGrandchild() {
		TestFhirTreeNode root = new TestFhirTreeNode("ROOT", "Test");
		TestFhirTreeNode childNode = new TestFhirTreeNode("CHILD", "Test.child");
		root.addChild(childNode);
		TestFhirTreeNode grandchildNode = new TestFhirTreeNode("GRANDCHILD", "Test.child.grandchild");
		childNode.addChild(grandchildNode);
		FhirTreeData<Object, TestFhirTreeNode> data = new FhirTreeData<>(root);

		Assert.assertEquals(3, Iterators.size(data.iterator()));
	}

	/* 
	 * ROOT
	 *  |
	 *  |---CHILD1
	 *  |   
	 *  |---CHILD2
	 */	
	@Test
	public void testIterateNodeWithTwoChildren() {
		TestFhirTreeNode root = new TestFhirTreeNode("ROOT", "Test");
		root.addChild(new TestFhirTreeNode("CHILD1", "Test.child1"));
		root.addChild(new TestFhirTreeNode("CHILD2", "Test.child2"));
		FhirTreeData<Object, TestFhirTreeNode> data = new FhirTreeData<>(root);

		Assert.assertEquals(3, Iterators.size(data.iterator()));
	}

	/*
	 * ROOT
	 *  |
	 *  |---CHILD1
	 *  |     |
	 *  |     |---GRANDCHILD1
	 *  |
	 *  |---CHILD2
	 *        |
	 *        |---GRANDCHILD2
	 */	
	@Test
	public void testIterateNodeWithTwoGrandchildren() {
		TestFhirTreeNode root = new TestFhirTreeNode("ROOT", "Test");
		TestFhirTreeNode child1 = new TestFhirTreeNode("CHILD1", "Test.child1");
		root.addChild(child1);
		TestFhirTreeNode grandchild1 = new TestFhirTreeNode("GRANDCHILD1", "Test.child1.grandchild1");
		child1.addChild(grandchild1);
		TestFhirTreeNode child2 = new TestFhirTreeNode("CHILD2", "Test.child2");
		root.addChild(child2);
		TestFhirTreeNode grandchild2 = new TestFhirTreeNode("GRANDCHILD2", "Test.child2.grandchild2");
		child2.addChild(grandchild2);
		FhirTreeData<Object, TestFhirTreeNode> data = new FhirTreeData<>(root);

		Assert.assertEquals(5, Iterators.size(data.iterator()));
	} 

	/*
	 * ROOT
	 *  |
	 *  |---CHILD1
	 *  |     |
	 *  |     |---GRANDCHILD1A
	 *  |     |		|
	 *  |     |		---GGRANDCHILD1
	 *  |     |
	 *  |     |---GRANDCHILD1B
	 *  |
	 *  |---CHILD2
	 *        |
	 *        |---GRANDCHILD2A
	 *        |
	 *        |---GRANDCHILD2B
	 */	
	@Test
	public void testIterateNodeWithGreatGrandchildren() {
		TestFhirTreeNode root = new TestFhirTreeNode("ROOT", "Test");
		TestFhirTreeNode child1 = new TestFhirTreeNode("CHILD1", "Test.child1");
		root.addChild(child1);
		TestFhirTreeNode grandchild1a = new TestFhirTreeNode("GRANDCHILD1A", "Test.child1.grandchild1a");
		child1.addChild(grandchild1a);
		TestFhirTreeNode ggrandchild1 = new TestFhirTreeNode("GGRANDCHILD1", "Test.child1.grandchild1a.ggrandchild1");
		grandchild1a.addChild(ggrandchild1);
		TestFhirTreeNode grandchild1b = new TestFhirTreeNode("GRANDCHILD1B", "Test.child1.grandchild1b");
		child1.addChild(grandchild1b);
		TestFhirTreeNode child2 = new TestFhirTreeNode("CHILD2", "Test.child2");
		root.addChild(child2);
		TestFhirTreeNode grandchild2a = new TestFhirTreeNode("GRANDCHILD2A", "Test.child2.grandchild2a");
		child2.addChild(grandchild2a);
		TestFhirTreeNode grandchild2b = new TestFhirTreeNode("GRANDCHILD2A", "Test.child2.grandchild2b");
		child2.addChild(grandchild2b);
		FhirTreeData<Object, TestFhirTreeNode> data = new FhirTreeData<>(root);

		Assert.assertEquals(8, Iterators.size(data.iterator()));
	} 
}

class TestFhirTreeNode extends TreeNode<Object, TestFhirTreeNode> {
	
	private final String id;
	
	public TestFhirTreeNode(String id, String path) {
		super(new Object(), new ImmutableNodePath(path));
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
}
