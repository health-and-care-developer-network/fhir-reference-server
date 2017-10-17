package uk.nhs.fhir.makehtml.render;

import javax.xml.parsers.ParserConfigurationException;

import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.makehtml.FhirFileRegistry;
import uk.nhs.fhir.util.FhirVersion;

public abstract class ResourceFormatter<T extends WrappedResource<T>> {
	
	protected final T wrappedResource;
	protected final FhirFileRegistry otherResources;
	
	public ResourceFormatter(T wrappedResource, FhirFileRegistry otherResources) {
		this.wrappedResource = wrappedResource;
		this.otherResources = otherResources;
	}
	
	public abstract HTMLDocSection makeSectionHTML() throws ParserConfigurationException;
	
	protected FhirVersion getResourceVersion() {
		return wrappedResource.getImplicitFhirVersion();
	}

}