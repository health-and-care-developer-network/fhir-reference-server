package uk.nhs.fhir.makehtml.data;

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
}
