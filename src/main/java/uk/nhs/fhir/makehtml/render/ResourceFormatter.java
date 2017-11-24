package uk.nhs.fhir.makehtml.render;

import javax.xml.parsers.ParserConfigurationException;

import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.util.FhirVersion;

public abstract class ResourceFormatter<T extends WrappedResource<T>> {
	
	protected final T wrappedResource;
	protected final RendererContext context;
	
	public ResourceFormatter(T wrappedResource, RendererContext context) {
		this.wrappedResource = wrappedResource;
		this.context = context;
	}
	
	public abstract HTMLDocSection makeSectionHTML() throws ParserConfigurationException;
	
	protected FhirVersion getResourceVersion() {
		return wrappedResource.getImplicitFhirVersion();
	}

}