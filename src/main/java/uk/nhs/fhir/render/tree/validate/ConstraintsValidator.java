package uk.nhs.fhir.render.tree.validate;

import java.util.List;
import java.util.Set;

import uk.nhs.fhir.data.structdef.ConstraintInfo;
import uk.nhs.fhir.data.wrap.HasConstraints;
import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;

public class ConstraintsValidator {

	private final HasConstraints elementDefinition;

	public ConstraintsValidator(HasConstraints elementDefinition) {
		this.elementDefinition = elementDefinition;
	}

	public void validate() {
		Set<String> conditionIds = elementDefinition.getConditionIds();
		
		List<ConstraintInfo> constraints = elementDefinition.getConstraintInfos();
		for (ConstraintInfo constraint : constraints) {
			String key = constraint.getKey();
			if (!conditionIds.contains(key)) {
				EventHandlerContext.forThread().event(RendererEventType.CONSTRAINT_WITHOUT_CONDITION, 
					"Constraint " + key + " doesn't have an associated condition pointing at it");
			}
		}

		//check for duplicate keys
		for (int i=0; i<constraints.size(); i++) {
			ConstraintInfo constraint1 = constraints.get(i);
			for (int j=i+1; j<constraints.size(); j++) {
				ConstraintInfo constraint2 = constraints.get(j);
				if (constraint1.getKey().equals(constraint2.getKey())) {
					EventHandlerContext.forThread().event(RendererEventType.DUPLICATE_CONSTRAINT_KEYS, 
						"Node constraints with duplicate keys: '" + constraint1.getKey() + "' for node "
						+ elementDefinition.getIdentifierString());
				}
			}
		}
	}
}
