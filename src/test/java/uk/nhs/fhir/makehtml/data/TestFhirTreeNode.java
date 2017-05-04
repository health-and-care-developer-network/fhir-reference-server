package uk.nhs.fhir.makehtml.data;

import java.util.Set;

import com.google.common.collect.Lists;

public class TestFhirTreeNode {
	public static FhirTreeNode testNode(String id, String path) {
		return new FhirTreeNode(
			new FhirTreeNodeId(id, FhirIcon.ELEMENT), 
			new ResourceFlags(), 
			0, 
			"*", 
			Lists.newArrayList(), 
			"", 
			Lists.newArrayList(), 
			path);
	}
	
	public static FhirTreeNode testSlicingNode(String id, String path, Set<String> discriminators) {
		FhirTreeNode node = testNode(id, path);
		node.setSlicingInfo(new SlicingInfo("Test desc", discriminators, Boolean.FALSE, "Test rules"));
		return node;
	}
}
