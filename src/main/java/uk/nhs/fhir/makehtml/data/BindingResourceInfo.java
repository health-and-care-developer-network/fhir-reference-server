package uk.nhs.fhir.makehtml.data;

import java.net.URL;
import java.util.Optional;

public class BindingResourceInfo extends ResourceInfo {

	public BindingResourceInfo(BindingInfo bindingInfo) {
		this(bindingInfo.getDescription(), bindingInfo.getUrl(), bindingInfo.getStrength());
	}

	public BindingResourceInfo(Optional<String> description, Optional<URL> url, String strength) {
		super("Binding", description, url, ResourceInfoType.BINDING);
		addExtraTag("Strength: " + strength);
	}
}
