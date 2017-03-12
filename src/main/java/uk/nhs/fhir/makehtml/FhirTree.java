package uk.nhs.fhir.makehtml;

import java.util.List;

import com.google.common.collect.Lists;

import uk.nhs.fhir.util.LinkData;

public class FhirTree {
	private final FhirTreeNode root;
	
	public FhirTree(String name, FhirIcon icon, LinkData typeLink, String value) {
		this.root = new FhirTreeNode(name, icon, "", typeLink, value, Lists.newArrayList());
	}
	
	public void addChild(FhirTreeNode child) {
		root.addChild(child);
	}
	
}

class FhirTreeNode {
	private String name;
	private FhirIcon icon;
	private String cardinality;
	private LinkData typeLink;
	private String value;
	private List<ResourceFlag> flags;
	
	private List<FhirTreeNode> children = Lists.newArrayList();

	public FhirTreeNode(String name, FhirIcon icon, String cardinality, LinkData typeLink, String value, List<ResourceFlag> flags) {
		this.name = name;
		this.icon = icon;
		this.cardinality = cardinality;
		this.typeLink = typeLink;
		this.value = value;
		this.flags = flags;
	}
	
	public void addChild(FhirTreeNode child) {
		children.add(child);
	}
}