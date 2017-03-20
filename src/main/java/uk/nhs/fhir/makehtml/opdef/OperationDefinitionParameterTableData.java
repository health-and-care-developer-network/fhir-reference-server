package uk.nhs.fhir.makehtml.opdef;

import java.util.List;

import uk.nhs.fhir.makehtml.data.ResourceInfo;
import uk.nhs.fhir.util.LinkData;

public class OperationDefinitionParameterTableData {
	
	private final String rowTitle;
	private final String cardinality;
	private final LinkData typeLink;
	private final String value;
	private final List<ResourceInfo> resourceFlags;
	
	public OperationDefinitionParameterTableData(
		String rowTitle, 
		String cardinality, 
		LinkData typeLink, 
		String value, 
		List<ResourceInfo> resourceFlags) {
		this.rowTitle = rowTitle;
		this.cardinality = cardinality;
		this.typeLink = typeLink;
		this.value = value;
		this.resourceFlags = resourceFlags;
	}
	
	public String getRowTitle() {
		return rowTitle;
	}
	
	public String getCardinality() {
		return cardinality;
	}
	
	public LinkData getTypeLink() {
		return typeLink;
	}
	
	public String getValue() {
		return value;
	}
	
	public List<ResourceInfo> getResourceFlags() {
		return resourceFlags;
	}
}