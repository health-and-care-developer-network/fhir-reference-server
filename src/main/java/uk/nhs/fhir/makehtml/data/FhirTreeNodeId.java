package uk.nhs.fhir.makehtml.data;

import java.net.URL;
import java.util.Optional;

import com.google.common.base.Preconditions;

public class FhirTreeNodeId {
	private final String name;
	private final Optional<URL> url;
	private FhirIcon icon;
	
	public FhirTreeNodeId(String name, URL url, FhirIcon icon) {
		Preconditions.checkNotNull(name, "FhirTreeNodeName name");
		Preconditions.checkNotNull(icon, "FhirTreeNodeName icon");
		
		this.name = name;
		this.url = Optional.ofNullable(url);
		this.icon = icon;
	}
	
	public String getName() {
		return name;
	}
	public Optional<URL> getUrl() {
		return url;
	}
	public FhirIcon getFhirIcon() {
		return icon;
	}

	public void setFhirIcon(FhirIcon icon) {
		this.icon = icon;
	}
}
