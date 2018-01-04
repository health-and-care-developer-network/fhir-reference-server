package uk.nhs.fhir.render.data;

import org.junit.Test;

import com.google.common.collect.Sets;

import junit.framework.Assert;
import uk.nhs.fhir.render.format.structdef.UnchangedSliceInfoRemover;
import uk.nhs.fhir.render.tree.AbstractFhirTreeTableContent;
import uk.nhs.fhir.render.tree.FhirTreeData;
import uk.nhs.fhir.render.tree.FhirTreeNode;

public class TestUnchangedSliceInfoRemover {
	@Test
	public void testHideOne() {
		FhirTreeNode root = TestFhirTreeNode.testSlicingNode("ROOT", "Test", Sets.newHashSet());
		root.addChild(TestFhirTreeNode.testNode("CHILD", "Test.child"));
		FhirTreeData<AbstractFhirTreeTableContent> data = new FhirTreeData<>(root);
		
		Assert.assertEquals(2, TestFhirTreeData.nodesCount(data));
		
		FhirTreeNode diffRoot = TestFhirTreeNode.testNode("ROOT", "Test");
		diffRoot.setBackupNode(root);
		
		UnchangedSliceInfoRemover remover = new UnchangedSliceInfoRemover(new FhirTreeData<>(diffRoot));
		remover.process(data);
		
		Assert.assertEquals(1, TestFhirTreeData.nodesCount(data));
	}
	
	@Test
	public void testKeepOne() {
		FhirTreeNode root = TestFhirTreeNode.testSlicingNode("ROOT", "Test", Sets.newHashSet());
		FhirTreeNode child = TestFhirTreeNode.testNode("CHILD", "Test.child");
		root.addChild(child);
		FhirTreeData<AbstractFhirTreeTableContent> data = new FhirTreeData<>(root);
		
		Assert.assertEquals(2, TestFhirTreeData.nodesCount(data));
		
		FhirTreeNode diffRoot = TestFhirTreeNode.testNode("ROOT", "Test");
		diffRoot.setBackupNode(root);
		FhirTreeNode diffChild = TestFhirTreeNode.testNode("CHILD", "Test.child");
		diffChild.setBackupNode(child);
		diffRoot.addChild(diffChild);
		
		UnchangedSliceInfoRemover remover = new UnchangedSliceInfoRemover(new FhirTreeData<>(diffRoot));
		remover.process(data);
		
		Assert.assertEquals(2, TestFhirTreeData.nodesCount(data));
	}
	
	@Test
	public void testHideGrandchild() {
		FhirTreeNode root = TestFhirTreeNode.testSlicingNode("ROOT", "Test", Sets.newHashSet());
		FhirTreeNode child = TestFhirTreeNode.testNode("CHILD", "Test.child");
		root.addChild(child);
		FhirTreeNode grandchild = TestFhirTreeNode.testNode("GRANDCHILD", "Test.child.grandchild");
		child.addChild(grandchild);
		FhirTreeData<AbstractFhirTreeTableContent> data = new FhirTreeData<>(root);
		
		Assert.assertEquals(3, TestFhirTreeData.nodesCount(data));
		
		FhirTreeNode diffRoot = TestFhirTreeNode.testNode("ROOT", "Test");
		diffRoot.setBackupNode(root);
		FhirTreeNode diffChild = TestFhirTreeNode.testNode("CHILD", "Test.child");
		diffChild.setBackupNode(child);
		diffRoot.addChild(diffChild);
		
		UnchangedSliceInfoRemover remover = new UnchangedSliceInfoRemover(new FhirTreeData<>(diffRoot));
		remover.process(data);
		
		Assert.assertEquals(2, TestFhirTreeData.nodesCount(data));
	}
	
	@Test
	public void testKeepGrandchild() {
		FhirTreeNode root = TestFhirTreeNode.testSlicingNode("ROOT", "Test", Sets.newHashSet());
		FhirTreeNode child = TestFhirTreeNode.testNode("CHILD", "Test.child");
		root.addChild(child);
		FhirTreeNode grandchild = TestFhirTreeNode.testNode("GRANDCHILD", "Test.child.grandchild");
		child.addChild(grandchild);
		FhirTreeData<AbstractFhirTreeTableContent> data = new FhirTreeData<>(root);
		
		Assert.assertEquals(3, TestFhirTreeData.nodesCount(data));
		
		FhirTreeNode diffRoot = TestFhirTreeNode.testNode("ROOT", "Test");
		diffRoot.setBackupNode(root);
		FhirTreeNode diffChild = TestFhirTreeNode.testNode("CHILD", "Test.child");
		diffChild.setBackupNode(child);
		diffRoot.addChild(diffChild);
		FhirTreeNode diffGrandchild = TestFhirTreeNode.testNode("GRANDCHILD", "Test.child.grandchild");
		diffGrandchild.setBackupNode(grandchild);
		diffChild.addChild(diffGrandchild);
		
		UnchangedSliceInfoRemover remover = new UnchangedSliceInfoRemover(new FhirTreeData<>(diffRoot));
		remover.process(data);
		
		Assert.assertEquals(3, TestFhirTreeData.nodesCount(data));
	}
	
	@Test
	public void testKeepGrandchildsParent() {
		FhirTreeNode root = TestFhirTreeNode.testSlicingNode("ROOT", "Test", Sets.newHashSet());
		FhirTreeNode child = TestFhirTreeNode.testNode("CHILD", "Test.child");
		root.addChild(child);
		FhirTreeNode grandchild = TestFhirTreeNode.testNode("GRANDCHILD", "Test.child.grandchild");
		child.addChild(grandchild);
		FhirTreeData<AbstractFhirTreeTableContent> data = new FhirTreeData<>(root);
		
		Assert.assertEquals(3, TestFhirTreeData.nodesCount(data));
		
		FhirTreeNode diffRoot = TestFhirTreeNode.testNode("ROOT", "Test");
		diffRoot.setBackupNode(root);
		FhirTreeNode diffChild = TestFhirTreeNode.testNode("CHILD", "Test.child");
		diffChild.setBackupNode(child);
		diffRoot.addChild(diffChild);
		FhirTreeNode diffGrandchild = TestFhirTreeNode.testNode("GRANDCHILD", "Test.child.grandchild");
		diffGrandchild.setBackupNode(grandchild);
		diffChild.addChild(diffGrandchild);
		
		UnchangedSliceInfoRemover remover = new UnchangedSliceInfoRemover(new FhirTreeData<>(diffRoot));
		remover.process(data);
		
		Assert.assertEquals(3, TestFhirTreeData.nodesCount(data));
	}
}
