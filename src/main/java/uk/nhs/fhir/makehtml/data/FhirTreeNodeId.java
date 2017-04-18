package uk.nhs.fhir.makehtml.data;

import com.google.common.base.Preconditions;

public class FhirTreeNodeId {
	private final String name;
	private FhirIcon icon;
	
	public FhirTreeNodeId(String name, FhirIcon icon) {
		Preconditions.checkNotNull(name, "FhirTreeNodeName name");
		Preconditions.checkNotNull(icon, "FhirTreeNodeName icon");
		
		this.name = name;
		this.icon = icon;
	}
	
	public String getName() {
		return name;
	}
	
	public FhirIcon getFhirIcon() {
		return icon;
	}
	public void setFhirIcon(FhirIcon icon) {
		this.icon = icon;
	}
}
