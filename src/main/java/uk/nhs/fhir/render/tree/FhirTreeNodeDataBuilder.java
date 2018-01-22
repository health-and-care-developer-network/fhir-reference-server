package uk.nhs.fhir.render.tree;

import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import uk.nhs.fhir.data.structdef.BindingInfo;
import uk.nhs.fhir.data.structdef.ConstraintInfo;
import uk.nhs.fhir.data.structdef.ExtensionType;
import uk.nhs.fhir.data.structdef.FhirElementDataType;
import uk.nhs.fhir.data.structdef.FhirElementMapping;
import uk.nhs.fhir.data.structdef.ResourceFlags;
import uk.nhs.fhir.data.structdef.SlicingInfo;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.util.FhirVersion;

public class FhirTreeNodeDataBuilder {
	protected final Optional<String> name;
	protected final ResourceFlags resourceFlags;
	protected final LinkDatas typeLinks;
	protected final String information;
	protected final List<ConstraintInfo> constraints;
	protected final String path;
	protected final FhirElementDataType dataType;
	protected final FhirVersion version;
	
	public FhirTreeNodeDataBuilder(
			Optional<String> name,
			ResourceFlags resourceFlags,
			LinkDatas typeLinks,
			String information,
			List<ConstraintInfo> constraints,
			String path,
			FhirElementDataType dataType,
			FhirVersion version) {
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
	
	protected Optional<String> id = Optional.empty();
	
	public FhirTreeNodeDataBuilder setId(Optional<String> id) {
		this.id = id;
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
	
	public DifferentialData toDifferentialData(SnapshotTreeNode backupNode) {
		return new DifferentialData(name, resourceFlags, Optional.ofNullable(min), Optional.ofNullable(max), typeLinks, information, constraints, path, dataType, version, backupNode);
	}
	
	public SnapshotData toSnapshotData() {
		return new SnapshotData(name, resourceFlags, min, max, typeLinks, information, constraints, path, dataType, version);
	}
}
