package uk.nhs.fhir.makehtml.data.structdef.tree;

import java.util.Optional;

import uk.nhs.fhir.makehtml.data.BindingInfo;
import uk.nhs.fhir.makehtml.data.ResourceInfo;
import uk.nhs.fhir.makehtml.data.ResourceInfoType;
import uk.nhs.fhir.makehtml.data.url.FhirURL;

public class BindingResourceInfo extends ResourceInfo {

	public BindingResourceInfo(BindingInfo bindingInfo) {
		this(bindingInfo.getDescription(), bindingInfo.getUrl(), bindingInfo.getStrength());
	}

	public BindingResourceInfo(Optional<String> description, Optional<FhirURL> url, String strength) {
		super("Binding", description, url, ResourceInfoType.BINDING, url.isPresent() && FhirURL.isLogicalUrl(url.get().toFullString()));
		addExtraTag("Strength: " + strength);
	}
}
