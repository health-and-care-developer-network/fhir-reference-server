package uk.nhs.fhir.makehtml.data;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.HTMLConstants;

public class FhirTreeSlicingNode extends FhirTreeNode {

	public FhirTreeSlicingNode(String slicedType, FhirCardinality cardinality, SlicingInfo slicingInfo, String path) {
		super(
			new FhirTreeNodeId("Slice (" + slicedType + ")", null, FhirIcon.SLICE),
			new ResourceFlags(),
			cardinality,
			Lists.newArrayList(new LinkData(HTMLConstants.HL7_DSTU2 + "/profiling.html#slicing", "Slice")),
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
