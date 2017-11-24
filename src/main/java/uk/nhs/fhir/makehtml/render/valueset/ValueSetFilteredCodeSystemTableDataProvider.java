package uk.nhs.fhir.makehtml.render.valueset;

import java.util.List;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.valueset.FhirValueSetComposeInclude;
import uk.nhs.fhir.data.valueset.FhirValueSetComposeIncludeFilter;
import uk.nhs.fhir.data.wrap.WrappedValueSet;
import uk.nhs.fhir.makehtml.html.table.TableTitle;
import uk.nhs.fhir.makehtml.render.RendererContext;

public class ValueSetFilteredCodeSystemTableDataProvider {
	
	private final WrappedValueSet valueSet;
	
	public ValueSetFilteredCodeSystemTableDataProvider(WrappedValueSet valueSet, RendererContext context) {
		
		if (valueSet.getCodeSystem().isPresent()) {
			throw new IllegalStateException("Didn't expect inline code system with a filter");
		}
		if (!valueSet.getConceptsToDisplay().isEmpty()) {
			throw new IllegalStateException("Didn't expect concepts to display with a filter");
		}
		if (!valueSet.getConceptMaps(context.getFhirFileRegistry()).isEmpty()) {
			throw new IllegalStateException("Didn't expect concepts map with a filter");
		}
		
		this.valueSet = valueSet;
	}
	
	List<TableTitle> getColumns() {
		return Lists.newArrayList(
			new TableTitle("Filter", "Selects codes/concepts by their properties", "25%"),
			new TableTitle("Property", "A property defined by the code system", "25%"),
			new TableTitle("Operation", "The kind of operation to perform", "25%"),
			new TableTitle("Value", "Code from the system, or regex criteria, or boolean value for exists", "25%")
		);
	}

	public List<ValueSetFilteredCodeSystemTableData> getRows() {
		List<ValueSetFilteredCodeSystemTableData> rows = Lists.newArrayList();
		
		for (FhirValueSetComposeInclude include : valueSet.getCompose().getIncludes()) {
			FhirURL system = FhirURL.buildOrThrow(include.getSystem(), valueSet.getImplicitFhirVersion());
			
			for (FhirValueSetComposeIncludeFilter filter : include.getFilters()) {
				String property = filter.getProperty();
				String operation = filter.getOp();
				String value = filter.getValue();
				
				rows.add(new ValueSetFilteredCodeSystemTableData(system, property, operation, value));
			}
		}
		
		return rows;
	}
	
}
