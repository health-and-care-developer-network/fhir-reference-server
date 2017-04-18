package uk.nhs.fhir.makehtml;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import uk.nhs.fhir.makehtml.data.FhirTreeData;
import uk.nhs.fhir.makehtml.data.FhirTreeNode;
import uk.nhs.fhir.makehtml.data.FhirTreeNodeBuilder;
import uk.nhs.fhir.makehtml.data.SlicingInfo;

public class DifferentialNodeBuilder extends FhirTreeNodeBuilder {

	private final FhirTreeData snapshotTree;
	
	public DifferentialNodeBuilder(FhirTreeData snapshotTree) {
		this.snapshotTree = snapshotTree;
	}
	
	private Optional<FhirTreeNode> findMatchingSnapshotNode(ElementDefinitionDt element) {
		List<FhirTreeNode> matchingNodes = findMatchingSnapshotNodes(element.getPath());
		
		if (matchingNodes.size() == 1) {
			return Optional.of(matchingNodes.get(0));
		} else if (matchingNodes.get(0).getSlicingInfo().isPresent()) {
			return matchOnDiscriminatorPaths(element, matchingNodes.get(0).getSlicingInfo().get());
		} else if (matchingNodes.size() == 0) {
			throw new IllegalStateException("No nodes matched for differential element path " + element.getPath());
		} else {
			throw new IllegalStateException("Multiple snapshot nodes matched differential element path " + element.getPath() + ", but first wasn't a slicing node");
		}
	}

	private Optional<FhirTreeNode> matchOnDiscriminatorPaths(ElementDefinitionDt element, SlicingInfo slicingInfo) {
		List<String> discriminatorPaths = slicingInfo.getDiscriminatorPaths();
		
		throw new IllegalStateException("Not yet implemented slicing in differential trees"); 
	}

	private List<FhirTreeNode> findMatchingSnapshotNodes(String differentialPath) {
		List<FhirTreeNode> matchingNodes = Lists.newArrayList();
		
		for (FhirTreeNode node : snapshotTree) {
			if (node.getPath().equals(differentialPath)) {
				matchingNodes.add(node);
			}
		}
		
		return matchingNodes;
	}
}
