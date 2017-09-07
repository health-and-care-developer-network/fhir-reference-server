package uk.nhs.fhir.makehtml.render.codesys;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.codesystem.FhirCodeSystemFilter;
import uk.nhs.fhir.makehtml.html.table.TableTitle;

public class CodeSystemFiltersTableDataProvider {
	private final List<FhirCodeSystemFilter> filters;
	
	public CodeSystemFiltersTableDataProvider(List<FhirCodeSystemFilter> filters) {
		this.filters = filters;
	}
	
	public List<TableTitle> getColumns() {

		boolean includeDisplayColumn = includeDisplayColumn();
		
		List<TableTitle> columns = Lists.newArrayList();
		
		columns.add(new TableTitle("Code", "Code that identifies the filter.", "20%"));
		
		columns.add(new TableTitle("Operators", "A list of operators that can be used with the filter.", "20%"));
		
		String valueWidth = includeDisplayColumn ? "30%" : "60%";
		columns.add(new TableTitle("Value", "What to use for the value.", valueWidth));
		
		if (includeDisplayColumn) {
			columns.add(new TableTitle("Description", "How or why the filter is used.", "30%"));
		}
		
		return columns;
	}

	public List<CodeSystemFilterTableRowData> getRows() {
		List<CodeSystemFilterTableRowData> data = Lists.newArrayList();
		
		for (FhirCodeSystemFilter filter : filters) {
			String code = filter.getCode();
			Optional<String> documentation = filter.getDocumentation();
			String operators = String.join(", ", filter.getPermittedOperators());
			String value = filter.getValue();

			data.add(new CodeSystemFilterTableRowData(code, operators, value, documentation));
		}
		
		return data;
	}
	
	public boolean includeDisplayColumn() {
		return includeDisplayColumn(getRows());
	}
	
	public boolean includeDisplayColumn(List<CodeSystemFilterTableRowData> rows) {
		return rows.stream().anyMatch(row -> row.hasDocumentation());
	}
}
