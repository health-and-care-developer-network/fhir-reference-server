package uk.nhs.fhir.render.tree;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import uk.nhs.fhir.data.structdef.ConstraintInfo;
import uk.nhs.fhir.data.structdef.FhirElementDataType;
import uk.nhs.fhir.data.structdef.ResourceFlags;
import uk.nhs.fhir.data.structdef.SlicingInfo;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.data.wrap.WrappedElementDefinition;
import uk.nhs.fhir.render.RendererContext;
import uk.nhs.fhir.render.tree.validate.ConstraintsValidator;
import uk.nhs.fhir.util.FhirFileRegistry;
import uk.nhs.fhir.util.FhirVersion;

public class FhirTreeNodeBuilder<T, U extends TreeNode<T, U>> {
	
	public SnapshotData buildSnapshotNode(WrappedElementDefinition elementDefinition) {
		return fromElementDefinition(elementDefinition).toSnapshotData();
	}
	
	public DifferentialData buildDifferentialNode(WrappedElementDefinition elementDefinition, SnapshotTreeNode backupNode) {
		return fromElementDefinition(elementDefinition).toDifferentialData(backupNode);
	}
	
	public FhirTreeNodeDataBuilder fromElementDefinition(WrappedElementDefinition elementDefinition) {
		
		Optional<String> name = Optional.ofNullable(elementDefinition.getName());

		LinkDatas typeLinks;
		if (elementDefinition.isRootElement()) {
			/*
			typeLinks = new LinkDatas(
				new LinkData(
					FhirURL.buildOrThrow(FhirURLConstants.HTTP_HL7_FHIR + "/profiling.html", elementDefinition.getVersion()), 
					"Profile"));*/
			typeLinks = new LinkDatas();
		} else {
			typeLinks = elementDefinition.getTypeLinks();
		}
		
		Set<FhirElementDataType> dataTypes = elementDefinition.getDataTypes();
		FhirElementDataType dataType;
		if (dataTypes.isEmpty()) {
			dataType = FhirElementDataType.DELEGATED_TYPE; 
		} else if (dataTypes.size() == 1) {
			dataType = dataTypes.iterator().next();
		} else if (dataTypes.size() > 1
		  && elementDefinition.getPath().endsWith("[x]")) {
			dataType = FhirElementDataType.CHOICE;
		} else {
			throw new IllegalStateException("Found " + dataTypes.size() + " data types for node " + elementDefinition.getPath());
		}

		ResourceFlags flags = elementDefinition.getResourceFlags();
		
		String shortDescription = elementDefinition.getShortDescription();
		if (shortDescription == null) {
			shortDescription = "";
		}
		
		List<ConstraintInfo> constraints = elementDefinition.getConstraintInfos();
		new ConstraintsValidator(elementDefinition).validate();
		
		String path = elementDefinition.getPath();
		FhirVersion version = elementDefinition.getVersion();

		FhirTreeNodeDataBuilder nodeBuilder = new FhirTreeNodeDataBuilder(
			name,
			flags,
			typeLinks, 
			shortDescription,
			constraints,
			path,
			dataType,
			version);

		Optional<String> definition = elementDefinition.getDefinition();
		if (definition.isPresent() && !definition.get().isEmpty()) {
			nodeBuilder.setDefinition(definition);
		}
		
		Optional<SlicingInfo> slicing = elementDefinition.getSlicing();
		if (slicing.isPresent()) {
			nodeBuilder.setSlicingInfo(slicing);	
		}
		
		nodeBuilder.setFixedValue(elementDefinition.getFixedValue());
		nodeBuilder.setExamples(elementDefinition.getExamples());
		nodeBuilder.setDefaultValue(elementDefinition.getDefaultValue());
		nodeBuilder.setBinding(elementDefinition.getBinding());
		
		Optional<String> requirements = elementDefinition.getRequirements();
		if (requirements.isPresent() && !requirements.get().isEmpty()) {
			nodeBuilder.setRequirements(requirements);
		}

		Optional<String> comments = elementDefinition.getComments();
		if (comments.isPresent() && !comments.get().isEmpty()) {
			nodeBuilder.setComments(comments);
		}

		nodeBuilder.setAliases(elementDefinition.getAliases());
		FhirFileRegistry fhirFileRegistry = RendererContext.forThread().getFhirFileRegistry();
		nodeBuilder.setExtensionType(elementDefinition.getExtensionType(fhirFileRegistry));
		
		Optional<String> nameReference = elementDefinition.getLinkedNodeName();
		if (nameReference.isPresent() && !nameReference.get().isEmpty()) {
			nodeBuilder.setLinkedNodeName(nameReference);
		}
		
		Optional<String> nodePath = elementDefinition.getLinkedNodePath();
		if (nodePath.isPresent() && !nodePath.get().isEmpty()) {
			nodeBuilder.setLinkedNodeId(nodePath);
		}

		new NodeMappingValidator(elementDefinition.getMappings(), elementDefinition.getPath()).validate();
		nodeBuilder.setMappings(elementDefinition.getMappings());
		
		nodeBuilder.setSliceName(elementDefinition.getSliceName());
		
		nodeBuilder.setId(elementDefinition.getId());
		
		return nodeBuilder;
	}

}
