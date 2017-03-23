package uk.nhs.fhir.makehtml.data;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

public class FhirTreeNode {
	private final FhirTreeNodeId id;
	private final ResourceFlags resourceFlags;
	private final FhirCardinality cardinality;
	private final List<LinkData> typeLinks;
	private final String information;
	private final List<ResourceInfo> constraints;
	private final String path;

	private Optional<SlicingInfo> slicingInfo = Optional.empty();
	private Optional<String> fixedValue = Optional.empty();
	private Optional<String> example = Optional.empty();
	private Optional<String> defaultValue = Optional.empty();
	private Optional<BindingInfo> binding = Optional.empty();
	
	private FhirTreeNode parent = null;
	
	private final List<FhirTreeNode> children = Lists.newArrayList();

	public FhirTreeNode(
			FhirTreeNodeId id,
			ResourceFlags flags, 
			FhirCardinality cardinality, 
			List<LinkData> typeLinks, 
			String information,
			List<ResourceInfo> constraints,
			String path) {
		this.id = id;
		this.resourceFlags = flags;
		this.cardinality = cardinality;
		this.typeLinks = typeLinks;
		this.information = information;
		this.constraints = constraints;
		this.path = path;
	}
	
	public FhirTreeNodeId getId() {
		return id;
	}
	
	public boolean hasSlicingInfo() {
		return slicingInfo.isPresent();
	}
	
	public Optional<SlicingInfo> getSlicingInfo() {
		return slicingInfo;
	}

	public void setSlicingInfo(SlicingInfo slicingInfo) {
		this.slicingInfo = Optional.ofNullable(slicingInfo);
	}
	
	public FhirCardinality getCardinality() {
		return cardinality;
	}
	
	public ResourceFlags getResourceFlags() {
		return resourceFlags;
	}
	
	public List<LinkData> getTypeLinks() {
		return typeLinks;
	}
	
	public String getInformation() {
		return information;
	}
	
	public void addChild(int index, FhirTreeNode child) {
		children.add(index, child);
		child.setParent(this);
	}
	
	public void addChild(FhirTreeNode child) {
		children.add(child);
		child.setParent(this);
	}
	
	public FhirTreeNode getParent() {
		return parent;
	}
	
	private void setParent(FhirTreeNode fhirTreeNode) {
		this.parent = fhirTreeNode;
	}
	
	public List<FhirTreeNode> getChildren() {
		return children;
	}
	
	public boolean hasChildren() {
		return children.size() > 0;
	}
	
	public boolean isRemovedByProfile() {
		return cardinality.getMax().equals(FhirElementCount.NONE);
	}
	
	public List<ResourceInfo> getConstraints() {
		return constraints;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getPathName() {
		String[] pathTokens = path.split("\\.");
		return pathTokens[pathTokens.length - 1];
	}

	public boolean isFixedValue() {
		return fixedValue.isPresent();
	}
	
	public Optional<String> getFixedValue() {
		return fixedValue;
	}

	public void setFixedValue(String fixedValue) {
		this.fixedValue = Optional.of(fixedValue);
	}

	public boolean hasExample() {
		return example.isPresent();
	}
	
	public Optional<String> getExample() {
		return example;
	}

	public void setExample(String exampleValue) {
		this.example = Optional.of(exampleValue);
	}

	public boolean hasDefaultValue() {
		return defaultValue.isPresent();
	}

	public Optional<String> getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = Optional.of(defaultValue);
	}

	public boolean hasBinding() {
		return binding.isPresent();
	}
	
	public Optional<BindingInfo> getBinding() {
		return binding;
	}
	
	public void setBinding(BindingInfo binding) {
		this.binding = Optional.of(binding);
	}
}