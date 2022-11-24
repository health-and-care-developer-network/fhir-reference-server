package uk.nhs.fhir.render.format.namingsystem;

import uk.nhs.fhir.data.url.LinkDatas;

public class NamingSystemIdentifierTableData {
	private final String rowValue;
	private final String type;
	private final Boolean preferred;
	
	
	public NamingSystemIdentifierTableData(String rowValue,	String type, Boolean preferred) 
	{
		this.rowValue = rowValue;
		this.type = type;
		this.preferred = preferred;
	}
	
	public String getRowValue() {
		return rowValue;
	}
	
	public String getType() {
		return type;
	}
	
	public Boolean getPreferred() {
		return preferred;
	}
	
	
}