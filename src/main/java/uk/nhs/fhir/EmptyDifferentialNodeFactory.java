package uk.nhs.fhir;

import uk.nhs.fhir.render.tree.DifferentialData;
import uk.nhs.fhir.render.tree.DifferentialTreeNode;
import uk.nhs.fhir.render.tree.EmptyNodeFactory;

public class EmptyDifferentialNodeFactory implements EmptyNodeFactory<DifferentialData, DifferentialTreeNode> {
	@Override
	public DifferentialTreeNode create(DifferentialTreeNode differentialNode, NodePath nodePath) {
		
	}
}
