package uk.nhs.fhir.makehtml.render;

import javax.xml.parsers.ParserConfigurationException;

import uk.nhs.fhir.makehtml.FhirVersion;
import uk.nhs.fhir.makehtml.data.wrap.WrappedResource;

public abstract class ResourceFormatter<T extends WrappedResource<T>> {
	
	protected final T wrappedResource;
	
	public ResourceFormatter(T wrappedResource) {
		this.wrappedResource = wrappedResource;
	}
	
	public abstract HTMLDocSection makeSectionHTML(T source) throws ParserConfigurationException;
	
	protected FhirVersion getResourceVersion() {
		return wrappedResource.getImplicitFhirVersion();
	}

}