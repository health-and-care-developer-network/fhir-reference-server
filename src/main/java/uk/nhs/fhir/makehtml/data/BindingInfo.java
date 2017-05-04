package uk.nhs.fhir.makehtml.data;

import java.net.URL;
import java.util.Optional;

import com.google.common.base.Preconditions;

public class BindingInfo {

	private final Optional<String> description;
	private final Optional<URL> url;
	private final String strength;

	public BindingInfo(Optional<String> description, Optional<URL> url, String strength) {
		this.description = description;
		this.url = url;
		this.strength = strength;
	}

	public Optional<String> getDescription() {
		return description;
	}
	
	public Optional<URL> getUrl() {
		return url;
	}
	
	public String getStrength() {
		return strength;
	}

	public static BindingInfo resolveWithBackupData(BindingInfo binding, BindingInfo backup) {
		Optional<String> bindingDescription = binding.getDescription();
		Optional<URL> bindingUrl = binding.getUrl();
		String bindingStrength = binding.getStrength();
		
		Optional<String> backupDescription = backup.getDescription();
		Optional<URL> backupUrl = backup.getUrl();
		String backupStrength = backup.getStrength();

		Optional<String> resolvedDescription = bindingDescription.isPresent() ? bindingDescription : backupDescription;
		Optional<URL> resolvedUrl = bindingUrl.isPresent() ? bindingUrl : backupUrl;
		String resolvedStrength = !bindingStrength.isEmpty() ? bindingStrength : backupStrength; 
		
		Preconditions.checkArgument(resolvedDescription.isPresent() || resolvedUrl.isPresent(), "Description or URL must be present");
		
		return new BindingInfo(resolvedDescription, resolvedUrl, resolvedStrength);
	}
}
