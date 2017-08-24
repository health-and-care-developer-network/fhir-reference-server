package uk.nhs.fhir.data.structdef.tree;

import java.util.Optional;

import uk.nhs.fhir.data.ResourceInfo;
import uk.nhs.fhir.data.ResourceInfoType;
import uk.nhs.fhir.data.structdef.BindingInfo;
import uk.nhs.fhir.data.url.FhirURL;

public class BindingResourceInfo extends ResourceInfo {

	public BindingResourceInfo(BindingInfo bindingInfo) {
		this(bindingInfo.getDescription(), bindingInfo.getUrl(), bindingInfo.getStrength());
	}

	public BindingResourceInfo(Optional<String> description, Optional<FhirURL> url, String strength) {
		super("Binding", description, url, ResourceInfoType.BINDING, url.isPresent() && FhirURL.isLogicalUrl(url.get().toFullString()));
		setQualifier("(" + strength + ")");
	}
}
