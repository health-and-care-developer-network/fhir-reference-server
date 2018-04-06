package uk.nhs.fhir.render.format.valueset;

import javax.xml.parsers.ParserConfigurationException;

import uk.nhs.fhir.data.wrap.WrappedValueSet;
import uk.nhs.fhir.render.RendererContext;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.TableFormatter;

public class ValueSetTableFormatter extends TableFormatter<WrappedValueSet> {
	
	public ValueSetTableFormatter(WrappedValueSet wrappedResource) {
		super(wrappedResource);
	}
	
	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		
		boolean filterPresent = wrappedResource.hasComposeIncludeFilter();
		boolean codesystemUnavailable = wrappedResource.hasUnavailableCodeSystem(RendererContext.forThread().getFhirFileRegistry());
		
		if (filterPresent) {
			return new FilteredValueSetTableFormatter(wrappedResource).makeSectionHTML();
		} else if (codesystemUnavailable) {
			return new ImportedValueSetTableFormatter(wrappedResource).makeSectionHTML();
		} else {
			return new ConceptsValueSetTableFormatter(wrappedResource).makeSectionHTML();
		}
	}
}
