package uk.nhs.fhir.makehtml.data;

import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.structdef.FhirElementDataType;
import uk.nhs.fhir.data.structdef.ResourceFlags;
import uk.nhs.fhir.data.structdef.SlicingInfo;
import uk.nhs.fhir.data.structdef.tree.FhirTreeNode;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.util.FhirVersion;

public class TestFhirTreeNode {
	public static FhirTreeNode testNode(String id, String path) {
		return new FhirTreeNode(
			Optional.of(id), 
			new ResourceFlags(), 
			0, 
			"*", 
			new LinkDatas(), 
			"", 
			Lists.newArrayList(), 
			path,
			FhirElementDataType.ELEMENT,
			FhirVersion.DSTU2);
	}
	
	public static FhirTreeNode testSlicingNode(String id, String path, Set<String> discriminators) {
		FhirTreeNode node = testNode(id, path);
		node.setSlicingInfo(Optional.of(new SlicingInfo("Test desc", discriminators, Boolean.FALSE, "Test rules")));
		return node;
	}
}
