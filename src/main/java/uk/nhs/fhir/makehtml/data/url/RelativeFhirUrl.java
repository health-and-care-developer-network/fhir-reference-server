package uk.nhs.fhir.makehtml.data.url;

import com.google.common.base.Preconditions;

public class RelativeFhirUrl extends FhirURL {

	private final String url;

	public RelativeFhirUrl(String url) {
		Preconditions.checkArgument(!url.contains("/") && !url.contains(" "));
		
		this.url = url;
	}

	@Override
	public String toLinkString() {
		return url;
	}

	@Override
	public String toFullString() {
		throw new IllegalStateException("Shouldn't be displaying a full string for a relative URL");
	}

}
