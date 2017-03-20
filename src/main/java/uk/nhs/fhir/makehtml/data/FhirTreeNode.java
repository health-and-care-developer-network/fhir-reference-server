package uk.nhs.fhir.makehtml.data;

import java.util.List;

import com.google.common.collect.Lists;

import uk.nhs.fhir.util.LinkData;

public class FhirTreeNode {
	private final FhirTreeNodeId id;
	private final ResourceFlags resourceFlags;
	private final FhirCardinality cardinality;
	private final LinkData typeLink;
	private final String information;
	private final List<ResourceInfo> resourceInfos;
	
	private FhirTreeNode parent = null;
	
	private final List<FhirTreeNode> children = Lists.newArrayList();

	public FhirTreeNode(
			FhirTreeNodeId id, 
			ResourceFlags flags, 
			FhirCardinality cardinality, 
			LinkData typeLink, 
			String information, 
			List<ResourceInfo> resourceInfos) {
		this.id = id;
		this.resourceFlags = flags;
		this.cardinality = cardinality;
		this.typeLink = typeLink;
		this.information = information;
		this.resourceInfos = resourceInfos;
	}
	
	public FhirTreeNodeId getId() {
		return id;
	}
	
	public FhirCardinality getCardinality() {
		return cardinality;
	}
	
	public ResourceFlags getResourceFlags() {
		return resourceFlags;
	}
	
	public LinkData getTypeLink() {
		return typeLink;
	}
	
	public String getInformation() {
		return information;
	}
	
	public List<ResourceInfo> getResourceInfos() {
		return resourceInfos;
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
}