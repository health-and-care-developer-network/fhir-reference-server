package uk.nhs.fhir.render.tree.tidy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.nhs.fhir.data.structdef.ConstraintInfo;
import uk.nhs.fhir.render.tree.AbstractFhirTreeTableContent;
import uk.nhs.fhir.render.tree.FhirTreeData;

/**
 * Tree tidier which removes constraints which are very common and not interesting.
 * If we do not remove them, the displayed tree will be very cluttered and information of interest will be harder to find.
 */
public class UnwantedConstraintRemover {

	private static final Set<String> constraintKeysToRemove = new HashSet<>(Arrays.asList(new String[] {"ele-1"}));
	
	private final FhirTreeData treeData;

	public UnwantedConstraintRemover(FhirTreeData treeData) {
		this.treeData = treeData;
	}

	public void process() {
		removeUnwantedConstraints(treeData.getRoot());
	}
	
	private void removeUnwantedConstraints(AbstractFhirTreeTableContent node) {
		for (AbstractFhirTreeTableContent child : node.getChildren()) {
			List<ConstraintInfo> constraints = child.getConstraints();
			for (int constraintIndex=constraints.size()-1; constraintIndex>=0; constraintIndex--) {
				ConstraintInfo constraint = constraints.get(constraintIndex);
				if (constraintKeysToRemove.contains(constraint.getKey())) {
					constraints.remove(constraintIndex);
				}
			}
			
			removeUnwantedConstraints(child);
		}
	}
}
