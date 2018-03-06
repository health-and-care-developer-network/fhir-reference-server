package uk.nhs.fhir.data.structdef.tree;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import uk.nhs.fhir.data.structdef.BindingInfo;
import uk.nhs.fhir.data.structdef.ConstraintInfo;
import uk.nhs.fhir.data.structdef.ExtensionType;
import uk.nhs.fhir.data.structdef.FhirElementDataType;
import uk.nhs.fhir.data.structdef.FhirElementMapping;
import uk.nhs.fhir.data.structdef.ResourceFlags;
import uk.nhs.fhir.data.structdef.SlicingInfo;
import uk.nhs.fhir.data.structdef.tree.validate.ConstraintsValidator;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.data.wrap.WrappedElementDefinition;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.StructureDefinitionRepository;

public class FhirTreeNodeDataBuilder {
	
	public static SnapshotData buildSnapshotNode(WrappedElementDefinition elementDefinition, Optional<StructureDefinitionRepository> structureDefinitions) {
		return fromElementDefinition(elementDefinition, structureDefinitions)
			.withDefinitionDetails(elementDefinition, structureDefinitions)
			.toSnapshotData();
	}
	
	public static DifferentialData buildDifferentialNode(WrappedElementDefinition elementDefinition, SnapshotTreeNode backupNode, Optional<StructureDefinitionRepository> structureDefinitions) {
		return fromElementDefinition(elementDefinition, structureDefinitions)
			.withDefinitionDetails(elementDefinition, structureDefinitions)
			.toDifferentialData(backupNode);
	}
	
	public static FhirDifferentialSkeletonData buildDifferentialSkeletonNode(WrappedElementDefinition elementDefinition, Optional<StructureDefinitionRepository> structureDefinitions) {
		return fromElementDefinition(elementDefinition, structureDefinitions)
			.withSkeletonDetails(elementDefinition)
			.toDifferentialSkeletonData();
	}

	private static FhirTreeNodeDataBuilder fromElementDefinition(WrappedElementDefinition elementDefinition, Optional<StructureDefinitionRepository> structureDefinitions) {
		
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
			typeLinks = elementDefinition.getTypeLinks(structureDefinitions);
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
		
		Optional<String> id = elementDefinition.getId();
		String path = elementDefinition.getPath();
		FhirVersion version = elementDefinition.getVersion();

		FhirTreeNodeDataBuilder nodeBuilder = new FhirTreeNodeDataBuilder(
			elementDefinition,
			id,
			name,
			flags,
			typeLinks, 
			shortDescription,
			constraints,
			path,
			dataType,
			version);
		
		return nodeBuilder;
	}
	
	private FhirTreeNodeDataBuilder withSkeletonDetails(WrappedElementDefinition elementDefinition) {
		setSlicingInfo(elementDefinition.getSlicing());
		setSliceName(elementDefinition.getSliceName());
		setFixedValue(elementDefinition.getFixedValue());
		
		return this;
	}
	
	private FhirTreeNodeDataBuilder withDefinitionDetails(
			WrappedElementDefinition elementDefinition,
			Optional<StructureDefinitionRepository> structureDefinitions) {

		if (elementDefinition.getCardinalityMin() != null) {
			setMin(elementDefinition.getCardinalityMin());
		}

		if (elementDefinition.getCardinalityMax() != null) {
			setMax(elementDefinition.getCardinalityMax());
		}
		

		Optional<String> definition = elementDefinition.getDefinition();
		if (definition.isPresent() && !definition.get().isEmpty()) {
			setDefinition(definition);
		}
		
		Optional<SlicingInfo> slicing = elementDefinition.getSlicing();
		if (slicing.isPresent()) {
			setSlicingInfo(slicing);	
		}
		
		setFixedValue(elementDefinition.getFixedValue());
		setExamples(elementDefinition.getExamples());
		setDefaultValue(elementDefinition.getDefaultValue());
		setBinding(elementDefinition.getBinding());
		
		Optional<String> requirements = elementDefinition.getRequirements();
		if (requirements.isPresent() && !requirements.get().isEmpty()) {
			setRequirements(requirements);
		}

		Optional<String> comments = elementDefinition.getComments();
		if (comments.isPresent() && !comments.get().isEmpty()) {
			setComments(comments);
		}

		setAliases(elementDefinition.getAliases());
		
		Optional<String> nameReference = elementDefinition.getLinkedNodeName();
		if (nameReference.isPresent() && !nameReference.get().isEmpty()) {
			setLinkedNodeName(nameReference);
		}
		
		Optional<String> nodePath = elementDefinition.getLinkedNodePath();
		if (nodePath.isPresent() && !nodePath.get().isEmpty()) {
			setLinkedNodeId(nodePath);
		}

		setMappings(elementDefinition.getMappings());
		
		setSliceName(elementDefinition.getSliceName());
		
		setExtensionType(elementDefinition.getExtensionType(structureDefinitions));
		
		return this;
	}

	protected final WrappedElementDefinition element;
	protected final Optional<String> id;
	protected final Optional<String> name;
	protected final ResourceFlags resourceFlags;
	protected final LinkDatas typeLinks;
	protected final String information;
	protected final List<ConstraintInfo> constraints;
	protected final String path;
	protected final FhirElementDataType dataType;
	protected final FhirVersion version;
	
	public FhirTreeNodeDataBuilder(
			WrappedElementDefinition element,
			Optional<String> id,
			Optional<String> name,
			ResourceFlags resourceFlags,
			LinkDatas typeLinks,
			String information,
			List<ConstraintInfo> constraints,
			String path,
			FhirElementDataType dataType,
			FhirVersion version) {
		this.element = element;
		this.id = id;
		this.name = name;
		this.resourceFlags = resourceFlags;
		this.typeLinks = typeLinks;
		this.information = information;
		this.constraints = constraints;
		this.path = path;
		this.dataType = dataType;
		this.version = version;
	}
	
	protected Integer min = null;
	
	public FhirTreeNodeDataBuilder setMin(Integer min) {
		this.min = Preconditions.checkNotNull(min);
		return this;
	}
	
	protected String max = null;
	
	public FhirTreeNodeDataBuilder setMax(String max) {
		this.max = Preconditions.checkNotNull(max);
		return this;
	}
	
	protected Optional<SnapshotTreeNode> linkedNode = Optional.empty();
	
	public FhirTreeNodeDataBuilder setLinkedNode(Optional<SnapshotTreeNode> linkedNode) {
		this.linkedNode = linkedNode;
		return this;
	}
	
	protected Optional<ExtensionType> extensionType = Optional.empty();
	
	public FhirTreeNodeDataBuilder setExtensionType(Optional<ExtensionType> extensionType) {
		this.extensionType = extensionType;
		return this;
	}
	
	protected Optional<SlicingInfo> slicingInfo = Optional.empty();
	
	public FhirTreeNodeDataBuilder setSlicingInfo(Optional<SlicingInfo> slicingInfo) {
		this.slicingInfo = slicingInfo;
		return this;
	}
	
	protected Optional<String> sliceName = Optional.empty();
	
	public FhirTreeNodeDataBuilder setSliceName(Optional<String> sliceName) {
		this.sliceName = sliceName;
		return this;
	}
	
	protected Optional<String> fixedValue = Optional.empty();
	
	public FhirTreeNodeDataBuilder setFixedValue(Optional<String> fixedValue) {
		this.fixedValue = fixedValue;
		return this;
	}
	
	protected List<String> examples = Lists.newArrayList();
	
	public FhirTreeNodeDataBuilder setExamples(List<String> examples) {
		this.examples = examples;
		return this;
	}
	
	protected Optional<String> defaultValue = Optional.empty();
	
	public FhirTreeNodeDataBuilder setDefaultValue(Optional<String> defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}
	
	protected Optional<BindingInfo> binding = Optional.empty();
	
	public FhirTreeNodeDataBuilder setBinding(Optional<BindingInfo> binding) {
		this.binding = binding;
		return this;
	}
	
	protected Optional<String> definition = Optional.empty();
	
	public FhirTreeNodeDataBuilder setDefinition(Optional<String> definition) {
		this.definition = definition;
		return this;
	}
	
	protected Optional<String> requirements = Optional.empty();
	
	public FhirTreeNodeDataBuilder setRequirements(Optional<String> requirements) {
		this.requirements = requirements;
		return this;
	}
	
	protected Optional<String> comments = Optional.empty();
	
	public FhirTreeNodeDataBuilder setComments(Optional<String> comments) {
		this.comments = comments;
		return this;
	}
	
	protected List<String> aliases = Lists.newArrayList();
	
	public FhirTreeNodeDataBuilder setAliases(List<String> aliases) {
		this.aliases = aliases;
		return this;
	}
	
	protected Optional<String> linkedNodeName = Optional.empty();
	
	public FhirTreeNodeDataBuilder setLinkedNodeName(Optional<String> linkedNodeName) {
		this.linkedNodeName = linkedNodeName;
		return this;
	}
	
	protected Optional<String> linkedNodeId = Optional.empty();
	
	public FhirTreeNodeDataBuilder setLinkedNodeId(Optional<String> linkedNodeId) {
		this.linkedNodeId = linkedNodeId;
		return this;
	}
	
	protected List<FhirElementMapping> mappings = Lists.newArrayList();
	
	public FhirTreeNodeDataBuilder setMappings(List<FhirElementMapping> mappings) {
		this.mappings = mappings;
		return this;
	}
	
	private DifferentialData toDifferentialData(SnapshotTreeNode backupNode) {
		DifferentialData data = new DifferentialData(id, name, resourceFlags, Optional.ofNullable(min), Optional.ofNullable(max), typeLinks, information, constraints, path, dataType, version, backupNode);
		data.setSlicingInfo(slicingInfo);
		data.setSliceName(sliceName);
		data.setFixedValue(fixedValue);
		data.setExtensionType(extensionType);
		data.setExamples(examples);
		data.setDefaultValue(defaultValue);
		data.setBinding(binding);
		data.setDefinition(definition);
		data.setRequirements(requirements);
		data.setComments(comments);
		data.setAliases(aliases);
		data.setLinkedNodeName(linkedNodeName);
		data.setLinkedNodeId(linkedNodeId);
		data.setMappings(mappings);
		
		return data;
	}
	
	private SnapshotData toSnapshotData() {
		SnapshotData data = new SnapshotData(id, name, resourceFlags, min, max, typeLinks, information, constraints, path, dataType, version);
		data.setSlicingInfo(slicingInfo);
		data.setSliceName(sliceName);
		data.setFixedValue(fixedValue);
		data.setExtensionType(extensionType);
		data.setExamples(examples);
		data.setDefaultValue(defaultValue);
		data.setBinding(binding);
		data.setDefinition(definition);
		data.setRequirements(requirements);
		data.setComments(comments);
		data.setAliases(aliases);
		data.setLinkedNodeName(linkedNodeName);
		data.setLinkedNodeId(linkedNodeId);
		data.setMappings(mappings);
		
		return data;
	}
	
	private FhirDifferentialSkeletonData toDifferentialSkeletonData() {
		return new FhirDifferentialSkeletonData(id, path, slicingInfo, sliceName, typeLinks, fixedValue, element);
	}
}
