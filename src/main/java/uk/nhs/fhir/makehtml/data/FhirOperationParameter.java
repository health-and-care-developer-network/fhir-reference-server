package uk.nhs.fhir.makehtml.data;

import java.util.List;

public class FhirOperationParameter {

	private final String name;
	private final Integer min;
	private final String max;
	private final LinkData typeLink;
	private final String documentation;
	private final List<ResourceInfo> resourceInfos;
	
	public FhirOperationParameter(String name, Integer min, String max, LinkData typeLink, String documentation, List<ResourceInfo> resourceInfos) {
		this.name = name;
		this.min = min;
		this.max = max;
		this.typeLink = typeLink;
		this.documentation = documentation;
		this.resourceInfos = resourceInfos;
	}
	
	public String getName() {
		return name;
	}

	public Integer getMin() {
		return min;
	}

	public String getMax() {
		return max;
	}

	public LinkData getTypeLink() {
		return typeLink;
	}

	public String getDocumentation() {
		return documentation;
	}

	public List<ResourceInfo> getResourceInfos() {
		return resourceInfos;
	}

}
