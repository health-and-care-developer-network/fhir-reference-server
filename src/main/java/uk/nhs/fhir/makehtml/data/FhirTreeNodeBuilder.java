package uk.nhs.fhir.makehtml.data;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.FhirURLConstants;
import uk.nhs.fhir.makehtml.data.wrap.WrappedElementDefinition;
import uk.nhs.fhir.makehtml.html.RendererError;
import uk.nhs.fhir.makehtml.valid.NodeMappingValidator;

public class FhirTreeNodeBuilder {
	
	public FhirTreeNode fromElementDefinition(WrappedElementDefinition elementDefinition) {
		
		Optional<String> name = Optional.ofNullable(elementDefinition.getName());

		List<LinkData> typeLinks = Lists.newArrayList();
		if (elementDefinition.isRootElement()) {
			typeLinks.add(new SimpleLinkData(FhirURL.buildOrThrow(FhirURLConstants.HTTP_HL7_DSTU2 + "/profiling.html"), "Profile"));
		} else {
			typeLinks.addAll(elementDefinition.getTypeLinks());
		}
		
		Set<FhirDataType> dataTypes = elementDefinition.getDataTypes();
		FhirDataType dataType;
		if (dataTypes.isEmpty()) {
			dataType = FhirDataType.DELEGATED_TYPE; 
		} else if (dataTypes.size() == 1) {
			dataType = dataTypes.iterator().next();
		} else if (dataTypes.size() > 1
		  && elementDefinition.getPath().endsWith("[x]")) {
			dataType = FhirDataType.CHOICE;
		} else {
			throw new IllegalStateException("Found " + dataTypes.size() + " data types for node " + elementDefinition.getPath());
		}

		ResourceFlags flags = elementDefinition.getResourceFlags();
		
		Integer min = elementDefinition.getCardinalityMin();
		String max = elementDefinition.getCardinalityMax();
		
		Optional<FhirDstu2Icon> icon = FhirDstu2Icon.forElementDefinition(elementDefinition);
		
		String shortDescription = elementDefinition.getShortDescription();
		if (shortDescription == null) {
			shortDescription = "";
		}
		
		Set<String> conditionIds = elementDefinition.getConditionIds();
		
		List<ConstraintInfo> constraints = elementDefinition.getConstraintInfos();
		
		for (ConstraintInfo constraint : constraints) {
			String key = constraint.getKey();
			if (!conditionIds.contains(key)) {
				RendererError.handle(RendererError.Key.CONSTRAINT_WITHOUT_CONDITION, "Constraint " + key + " doesn't have an associated condition pointing at it");
			}
		}

		//check for duplicate keys
		for (int i=0; i<constraints.size(); i++) {
			ConstraintInfo constraint1 = constraints.get(i);
			for (int j=i+1; j<constraints.size(); j++) {
				ConstraintInfo constraint2 = constraints.get(j);
				if (constraint1.getKey().equals(constraint2.getKey())) {
					RendererError.handle(RendererError.Key.DUPLICATE_CONSTRAINT_KEYS, "Node constraints with duplicate keys: '" + constraint1.getKey() + "'");
				}
			}
		}
		
		String path = elementDefinition.getPath();

		FhirTreeNode node = new FhirTreeNode(
			icon,
			name,
			flags,
			min,
			max,
			typeLinks, 
			shortDescription,
			constraints,
			path,
			dataType);

		Optional<String> definition = elementDefinition.getDefinition();
		if (definition.isPresent() && !definition.get().isEmpty()) {
			node.setDefinition(definition);
		}
		
		Optional<SlicingInfo> slicing = elementDefinition.getSlicing();
		if (slicing.isPresent()) {
			node.setSlicingInfo(slicing);	
		}
		
		node.setFixedValue(elementDefinition.getFixedValue());
		node.setExample(elementDefinition.getExample());
		node.setDefaultValue(elementDefinition.getDefaultValue());
		node.setBinding(elementDefinition.getBinding());
		
		Optional<String> requirements = elementDefinition.getRequirements();
		if (requirements.isPresent() && !requirements.get().isEmpty()) {
			node.setRequirements(requirements);
		}

		Optional<String> comments = elementDefinition.getComments();
		if (comments.isPresent() && !comments.get().isEmpty()) {
			node.setComments(comments);
		}

		node.setAliases(elementDefinition.getAliases());
		node.setExtensionType(elementDefinition.getExtensionType());
		
		
		Optional<String> nameReference = elementDefinition.getLinkedNodeName();
		if (nameReference.isPresent() && !nameReference.get().isEmpty()) {
			node.setLinkedNodeName(nameReference);
		}
		
		node.setMappings(elementDefinition.getMappings());
		NodeMappingValidator.validate(node);
		
		return node;
	}

}
