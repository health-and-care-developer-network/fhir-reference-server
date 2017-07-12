package uk.nhs.fhir.makehtml.render;

import javax.xml.parsers.ParserConfigurationException;

import uk.nhs.fhir.makehtml.data.ResourceSectionType;
import uk.nhs.fhir.makehtml.data.wrap.WrappedResource;
import uk.nhs.fhir.util.Dstu2FhirDocLinkFactory;

public abstract class ResourceFormatter<T extends WrappedResource<T>> {
	public ResourceSectionType resourceSectionType = ResourceSectionType.TREEVIEW;
	
	public abstract HTMLDocSection makeSectionHTML(T source) throws ParserConfigurationException;

	protected final Dstu2FhirDocLinkFactory fhirDocLinkFactory = new Dstu2FhirDocLinkFactory();
}