package uk.nhs.fhir.render.data;

import org.junit.Test;

import junit.framework.Assert;
import uk.nhs.fhir.render.tree.FhirTreeData;
import uk.nhs.fhir.render.tree.FhirTreeNode;
import uk.nhs.fhir.render.tree.FhirTreeTableContent;

public class TestFhirTreeData {
	
	public static int nodesCount(FhirTreeData data) {
		int total = 0;
		
		for (FhirTreeTableContent node : data) {
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
		FhirTreeNode root = TestFhirTreeNode.testNode("ROOT", "Test");
		FhirTreeData data = new FhirTreeData(root);
		
		Assert.assertEquals(1, nodesCount(data));
	}

	/*
	 * ROOT
	 *  |---|
	 *    CHILD
	 */
	@Test
	public void testIterateNodeWithChild() {
		FhirTreeNode root = TestFhirTreeNode.testNode("ROOT", "Test");
		root.addChild(TestFhirTreeNode.testNode("CHILD", "Test.child"));
		FhirTreeData data = new FhirTreeData(root);

		Assert.assertEquals(2, nodesCount(data));
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
		FhirTreeNode root = TestFhirTreeNode.testNode("ROOT", "Test");
		FhirTreeNode childNode = TestFhirTreeNode.testNode("CHILD", "Test.child");
		root.addChild(childNode);
		FhirTreeNode grandchildNode = TestFhirTreeNode.testNode("GRANDCHILD", "Test.child.grandchild");
		childNode.addChild(grandchildNode);
		FhirTreeData data = new FhirTreeData(root);

		Assert.assertEquals(3, nodesCount(data));
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
		FhirTreeNode root = TestFhirTreeNode.testNode("ROOT", "Test");
		root.addChild(TestFhirTreeNode.testNode("CHILD1", "Test.child1"));
		root.addChild(TestFhirTreeNode.testNode("CHILD2", "Test.child2"));
		FhirTreeData data = new FhirTreeData(root);

		Assert.assertEquals(3, nodesCount(data));
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
		FhirTreeNode root = TestFhirTreeNode.testNode("ROOT", "Test");
		FhirTreeNode child1 = TestFhirTreeNode.testNode("CHILD1", "Test.child1");
		root.addChild(child1);
		FhirTreeNode grandchild1 = TestFhirTreeNode.testNode("GRANDCHILD1", "Test.child1.grandchild1");
		child1.addChild(grandchild1);
		FhirTreeNode child2 = TestFhirTreeNode.testNode("CHILD2", "Test.child2");
		root.addChild(child2);
		FhirTreeNode grandchild2 = TestFhirTreeNode.testNode("GRANDCHILD2", "Test.child2.grandchild2");
		child2.addChild(grandchild2);
		FhirTreeData data = new FhirTreeData(root);

		Assert.assertEquals(5, nodesCount(data));
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
		FhirTreeNode root = TestFhirTreeNode.testNode("ROOT", "Test");
		FhirTreeNode child1 = TestFhirTreeNode.testNode("CHILD1", "Test.child1");
		root.addChild(child1);
		FhirTreeNode grandchild1a = TestFhirTreeNode.testNode("GRANDCHILD1A", "Test.child1.grandchild1a");
		child1.addChild(grandchild1a);
		FhirTreeNode ggrandchild1 = TestFhirTreeNode.testNode("GGRANDCHILD1", "Test.child1.grandchild1a.ggrandchild1");
		grandchild1a.addChild(ggrandchild1);
		FhirTreeNode grandchild1b = TestFhirTreeNode.testNode("GRANDCHILD1B", "Test.child1.grandchild1b");
		child1.addChild(grandchild1b);
		FhirTreeNode child2 = TestFhirTreeNode.testNode("CHILD2", "Test.child2");
		root.addChild(child2);
		FhirTreeNode grandchild2a = TestFhirTreeNode.testNode("GRANDCHILD2A", "Test.child2.grandchild2a");
		child2.addChild(grandchild2a);
		FhirTreeNode grandchild2b = TestFhirTreeNode.testNode("GRANDCHILD2A", "Test.child2.grandchild2b");
		child2.addChild(grandchild2b);
		FhirTreeData data = new FhirTreeData(root);

		Assert.assertEquals(8, nodesCount(data));
	} 
}
