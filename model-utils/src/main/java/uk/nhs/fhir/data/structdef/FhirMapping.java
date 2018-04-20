package uk.nhs.fhir.data.structdef;

import java.util.Optional;

import com.google.common.base.Preconditions;

public class FhirMapping {
	private final String identity;
	private final Optional<String> uri;
	private final Optional<String> name;
	private final Optional<String> comments;
	
	public FhirMapping(String identity, String mappingUri, String mappingName, String mappingComments) {
		this.identity = Preconditions.checkNotNull(identity);
		this.uri = Optional.ofNullable(mappingUri);
		this.name = Optional.ofNullable(mappingName);
		this.comments = Optional.ofNullable(mappingComments);
	}

	public String getIdentity() {
		return identity;
	}

	public Optional<String> getUri() {
		return uri;
	}

	public Optional<String> getName() {
		return name;
	}

	public Optional<String> getComments() {
		return comments;
	}
	
}
