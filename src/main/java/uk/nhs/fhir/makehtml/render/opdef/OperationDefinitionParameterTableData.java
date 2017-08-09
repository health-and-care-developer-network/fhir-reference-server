package uk.nhs.fhir.makehtml.render.opdef;

import java.util.List;

import uk.nhs.fhir.data.ResourceInfo;
import uk.nhs.fhir.data.url.LinkDatas;

public class OperationDefinitionParameterTableData {
	
	private final String rowTitle;
	private final String cardinality;
	private final LinkDatas typeLink;
	private final String value;
	private final List<ResourceInfo> resourceFlags;
	
	public OperationDefinitionParameterTableData(
		String rowTitle, 
		String cardinality, 
		LinkDatas typeLink, 
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
	
	public LinkDatas getTypeLink() {
		return typeLink;
	}
	
	public String getValue() {
		return value;
	}
	
	public List<ResourceInfo> getResourceFlags() {
		return resourceFlags;
	}
}