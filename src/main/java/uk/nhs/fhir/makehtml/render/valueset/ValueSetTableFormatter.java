package uk.nhs.fhir.makehtml.render.valueset;

import javax.xml.parsers.ParserConfigurationException;

import uk.nhs.fhir.data.wrap.WrappedValueSet;
import uk.nhs.fhir.makehtml.html.table.TableFormatter;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;

public class ValueSetTableFormatter extends TableFormatter<WrappedValueSet> {
	
	public ValueSetTableFormatter(WrappedValueSet wrappedResource) {
		super(wrappedResource);
	}
	
	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		
		boolean filterPresent = wrappedResource.hasComposeIncludeFilter();
		if (filterPresent) {
			return new FilteredValueSetTableFormatter(wrappedResource).makeSectionHTML();
		} else {
			return new ConceptsValueSetTableFormatter(wrappedResource).makeSectionHTML();
		}
	}
}
