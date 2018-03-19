package uk.nhs.fhir.render.data;

import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

import uk.nhs.fhir.data.structdef.SlicingInfo;
import uk.nhs.fhir.data.structdef.tree.FhirTreeData;
import uk.nhs.fhir.data.structdef.tree.ImmutableNodePath;
import uk.nhs.fhir.data.structdef.tree.TreeNode;
import uk.nhs.fhir.data.structdef.tree.tidy.HasBackupNode;
import uk.nhs.fhir.data.structdef.tree.tidy.HasSlicingInfo;
import uk.nhs.fhir.data.structdef.tree.tidy.UnchangedSliceInfoRemover;

public class TestUnchangedSliceInfoRemover {
	@Test
	public void testHideOne() {
		TestSnapshotNode root = new TestSnapshotNode("ROOT", "Test");
		root.addChild(new TestSnapshotNode("CHILD", "Test.child"));
		FhirTreeData<TestSnapshotData, TestSnapshotNode> data = new FhirTreeData<>(root);
		
		Assert.assertEquals(2, Iterators.size(data.iterator()));
		
		TestDifferentialNode diffRoot = new TestDifferentialNode("ROOT", "Test", root);
		 
		new UnchangedSliceInfoRemover<>(new FhirTreeData<>(diffRoot)).process(data);
		
		Assert.assertEquals(1, Iterators.size(data.iterator()));
	}
	
	@Test
	public void testKeepOne() {
		TestSnapshotNode root = new TestSnapshotNode("ROOT", "Test");
		TestSnapshotNode child = new TestSnapshotNode("CHILD", "Test.child");
		root.addChild(child);
		FhirTreeData<TestSnapshotData, TestSnapshotNode> data = new FhirTreeData<>(root);
		
		Assert.assertEquals(2, Iterators.size(data.iterator()));
		
		TestDifferentialNode diffRoot = new TestDifferentialNode("ROOT", "Test", root);
		TestDifferentialNode diffChild = new TestDifferentialNode("CHILD", "Test.child", child);
		diffRoot.addChild(diffChild);
		
		new UnchangedSliceInfoRemover<>(new FhirTreeData<>(diffRoot)).process(data);
		
		Assert.assertEquals(2, Iterators.size(data.iterator()));
	}
	
	@Test
	public void testHideGrandchild() {
		TestSnapshotNode root = new TestSnapshotNode("ROOT", "Test", Sets.newHashSet());
		TestSnapshotNode child = new TestSnapshotNode("CHILD", "Test.child");
		root.addChild(child);
		TestSnapshotNode grandchild = new TestSnapshotNode("GRANDCHILD", "Test.child.grandchild");
		child.addChild(grandchild);
		FhirTreeData<TestSnapshotData, TestSnapshotNode> data = new FhirTreeData<>(root);
		
		Assert.assertEquals(3, Iterators.size(data.iterator()));
		
		TestDifferentialNode diffRoot = new TestDifferentialNode("ROOT", "Test", root);
		TestDifferentialNode diffChild = new TestDifferentialNode("CHILD", "Test.child", child);
		diffRoot.addChild(diffChild);
		
		new UnchangedSliceInfoRemover<>(new FhirTreeData<>(diffRoot)).process(data);
		
		Assert.assertEquals(2, Iterators.size(data.iterator()));
	}
	
	@Test
	public void testKeepGrandchild() {
		TestSnapshotNode root = new TestSnapshotNode("ROOT", "Test", Sets.newHashSet());
		TestSnapshotNode child = new TestSnapshotNode("CHILD", "Test.child");
		root.addChild(child);
		TestSnapshotNode grandchild = new TestSnapshotNode("GRANDCHILD", "Test.child.grandchild");
		child.addChild(grandchild);
		FhirTreeData<TestSnapshotData, TestSnapshotNode> data = new FhirTreeData<>(root);
		
		Assert.assertEquals(3, Iterators.size(data.iterator()));
		
		TestDifferentialNode diffRoot = new TestDifferentialNode("ROOT", "Test", root);
		TestDifferentialNode diffChild = new TestDifferentialNode("CHILD", "Test.child", child);
		diffRoot.addChild(diffChild);
		TestDifferentialNode diffGrandchild = new TestDifferentialNode("GRANDCHILD", "Test.child.grandchild", grandchild);
		diffChild.addChild(diffGrandchild);
		
		new UnchangedSliceInfoRemover<>(new FhirTreeData<>(diffRoot)).process(data);
		
		Assert.assertEquals(3, Iterators.size(data.iterator()));
	}
	
	@Test
	public void testKeepGrandchildsParent() {
		TestSnapshotNode root = new TestSnapshotNode("ROOT", "Test", Sets.newHashSet());
		TestSnapshotNode child = new TestSnapshotNode("CHILD", "Test.child");
		root.addChild(child);
		TestSnapshotNode grandchild = new TestSnapshotNode("GRANDCHILD", "Test.child.grandchild");
		child.addChild(grandchild);
		FhirTreeData<TestSnapshotData, TestSnapshotNode> data = new FhirTreeData<>(root);
		
		Assert.assertEquals(3, Iterators.size(data.iterator()));
		
		TestDifferentialNode diffRoot = new TestDifferentialNode("ROOT", "Test", root);
		TestDifferentialNode diffChild = new TestDifferentialNode("CHILD", "Test.child", child);
		diffRoot.addChild(diffChild);
		TestDifferentialNode diffGrandchild = new TestDifferentialNode("GRANDCHILD", "Test.child.grandchild", grandchild);
		diffChild.addChild(diffGrandchild);
		
		new UnchangedSliceInfoRemover<>(new FhirTreeData<>(diffRoot)).process(data);
		
		Assert.assertEquals(3, Iterators.size(data.iterator()));
	}
}

class TestSnapshotData implements HasSlicingInfo {

	private Optional<SlicingInfo> slicingInfo;
	
	public TestSnapshotData(Set<String> discriminators) {
		this.slicingInfo = Optional.of(new SlicingInfo("Test desc", discriminators, Boolean.FALSE, "Test rules"));
	}
	
	@Override
	public boolean hasSlicingInfo() {
		return slicingInfo.isPresent();
	}

	@Override
	public Optional<SlicingInfo> getSlicingInfo() {
		return slicingInfo;
	}
}

class TestSnapshotNode extends TreeNode<TestSnapshotData, TestSnapshotNode> {

	private final String id;

	public TestSnapshotNode(String id, String path) {
		this(id, path, Sets.newHashSet());
	}
	
	public TestSnapshotNode(String id, String path, Set<String> discriminators) {
		super(new TestSnapshotData(discriminators), new ImmutableNodePath(path));
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
}

class TestDifferentialData implements HasBackupNode<TestSnapshotData, TestSnapshotNode>{

	private final TestSnapshotNode backupNode;
	
	public TestDifferentialData(TestSnapshotNode backupNode) {
		this.backupNode = backupNode;
	}

	@Override
	public TestSnapshotNode getBackupNode() {
		return backupNode;
	}
}

class TestDifferentialNode extends TreeNode<TestDifferentialData, TestDifferentialNode> {

	private final String id;

	public TestDifferentialNode(String id, String path, TestSnapshotNode backupNode) {
		super(new TestDifferentialData(backupNode), new ImmutableNodePath(path));
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
}
