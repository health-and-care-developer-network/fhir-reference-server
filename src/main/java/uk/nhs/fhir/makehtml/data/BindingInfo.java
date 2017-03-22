package uk.nhs.fhir.makehtml.data;

import java.net.URL;
import java.util.Optional;

import com.google.common.base.Preconditions;

import uk.nhs.fhir.makehtml.NewMain;

public class BindingInfo {

	private final Optional<String> description;
	private final Optional<URL> url;
	private final String strength;

	public BindingInfo(Optional<String> description, Optional<URL> url, String strength) {
		if (NewMain.STRICT) {
			Preconditions.checkArgument(description.isPresent() || url.isPresent(), "Description or URL must be present");
		} else if (!(description.isPresent() || url.isPresent())) {
			description = Optional.of("");
		}
		
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
}
