package uk.nhs.fhir.data.structdef;

import java.util.Optional;

import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.makehtml.EventHandlerContext;
import uk.nhs.fhir.makehtml.RendererEventType;

public class BindingInfo {

	public static final String STAND_IN_DESCRIPTION = "STAND IN STRING BECAUSE IT'S MISSING FROM THE SNAPSHOT";
	
	private final Optional<String> description;
	private final Optional<FhirURL> url;
	private final String strength;

	public BindingInfo(Optional<String> description, Optional<FhirURL> url, String strength) {
		this.description = description;
		this.url = url;
		this.strength = strength;
	}

	public Optional<String> getDescription() {
		return description;
	}
	
	public Optional<FhirURL> getUrl() {
		return url;
	}
	
	public String getStrength() {
		return strength;
	}

	public static BindingInfo resolveWithBackupData(BindingInfo binding, BindingInfo backup) {
		Optional<String> bindingDescription = binding.getDescription();
		Optional<FhirURL> bindingUrl = binding.getUrl();
		String bindingStrength = binding.getStrength();
		
		Optional<String> backupDescription = backup.getDescription();
		Optional<FhirURL> backupUrl = backup.getUrl();
		String backupStrength = backup.getStrength();

		Optional<String> resolvedDescription = bindingDescription.isPresent() ? bindingDescription : backupDescription;
		Optional<FhirURL> resolvedUrl = bindingUrl.isPresent() ? bindingUrl : backupUrl;
		String resolvedStrength = !bindingStrength.isEmpty() ? bindingStrength : backupStrength; 
		
		if (!resolvedDescription.isPresent() 
		  && !resolvedUrl.isPresent()) {
			EventHandlerContext.forThread().event(RendererEventType.BINDING_WITHOUT_DESC_OR_URL, "Description or URL must be present");
			resolvedDescription = Optional.of(STAND_IN_DESCRIPTION);
		}
		
		return new BindingInfo(resolvedDescription, resolvedUrl, resolvedStrength);
	}
	
	public int hashCode() {
		return description.hashCode() + url.hashCode() + strength.hashCode();
	}
	
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (!(other instanceof BindingInfo)) {
			return false;
		}
		
		BindingInfo otherBindingInfo = (BindingInfo)other;
		return description.equals(otherBindingInfo.getDescription())
			&& url.equals(otherBindingInfo.getUrl())
			&& strength.equals(otherBindingInfo.getStrength());
	}
}
