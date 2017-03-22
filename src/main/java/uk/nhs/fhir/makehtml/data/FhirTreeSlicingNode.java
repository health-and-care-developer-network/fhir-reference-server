package uk.nhs.fhir.makehtml.data;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class FhirTreeSlicingNode extends FhirTreeNode {

	public FhirTreeSlicingNode(String slicedType, FhirCardinality cardinality, SlicingInfo slicingInfo, String path) {
		super(
			new FhirTreeNodeId("Slice (" + slicedType + ")", null, FhirIcon.SLICE),
			new ResourceFlags(),
			cardinality,
			new LinkData("http://hl7.org/fhir/profiling.html#slicing", "Slicing"),
			"",
			Lists.newArrayList(),
			path);
		
		Preconditions.checkNotNull("Slicing info", slicingInfo);
		setSlicingInfo(slicingInfo);
	}

	public FhirTreeSlicingNode(FhirTreeNode nodeWithSlicing) {
		this(
			nodeWithSlicing.getId().getName(), 
			nodeWithSlicing.getCardinality(), 
			nodeWithSlicing.getSlicingInfo().get(), 
			nodeWithSlicing.getPath());
	}
}
