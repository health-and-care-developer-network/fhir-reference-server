package uk.nhs.fhir.data.namingsystem;


import java.util.List;
import java.util.Optional;

import uk.nhs.fhir.data.ResourceInfo;
import uk.nhs.fhir.data.url.LinkData;
import uk.nhs.fhir.data.url.LinkDatas;

public class FhirNamingSystemUniqueId {
	
	private final String type;
	private final String value ;
	private final Boolean preferred ;
	private final String comment ;
	//private final String period;
	
	
	public FhirNamingSystemUniqueId(String type, String value, Boolean preferred, String comment) {
		this.type = type;
		this.value = value;
		this.preferred = preferred;
		this.comment = comment;
		//this.period = period;
	}
	
	public String getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public Boolean getPreferred() {
		return preferred;
	}
	
	public String getComment() {
		return comment;
	}
/*
	public String getPeriod() {
		return period;
	}*/


}