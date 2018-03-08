package uk.nhs.fhir.data.opdef;

import java.util.List;
import java.util.Optional;

import uk.nhs.fhir.data.ResourceInfo;
import uk.nhs.fhir.data.url.LinkData;

public class FhirOperationParameter {

	private final String name;
	private final Integer min;
	private final String max;
	private final Optional<LinkData> typeLink;
	private final String documentation;
	private final List<ResourceInfo> resourceInfos;
	
	public FhirOperationParameter(String name, Integer min, String max, Optional<LinkData> typeLink, String documentation, List<ResourceInfo> resourceInfos) {
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

	public Optional<LinkData> getTypeLink() {
		return typeLink;
	}

	public String getDocumentation() {
		return documentation;
	}

	public List<ResourceInfo> getResourceInfos() {
		return resourceInfos;
	}

}
