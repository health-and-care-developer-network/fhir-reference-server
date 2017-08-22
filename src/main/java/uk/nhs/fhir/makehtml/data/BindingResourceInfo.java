package uk.nhs.fhir.makehtml.data;

import java.util.Optional;

public class BindingResourceInfo extends ResourceInfo {

	public BindingResourceInfo(BindingInfo bindingInfo) {
		this(bindingInfo.getDescription(), bindingInfo.getUrl(), bindingInfo.getStrength());
	}

	public BindingResourceInfo(Optional<String> description, Optional<FhirURL> url, String strength) {
		super("Binding", description, url, ResourceInfoType.BINDING, url.isPresent() && FhirURL.isLogicalUrl(url.get().toFullString()));
		addExtraTag("Strength: " + strength);
	}
}
