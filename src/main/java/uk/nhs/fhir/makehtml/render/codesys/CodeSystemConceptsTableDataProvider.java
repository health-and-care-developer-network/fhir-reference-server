package uk.nhs.fhir.makehtml.render.codesys;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.codesystem.FhirCodeSystemConcept;
import uk.nhs.fhir.data.codesystem.FhirCodeSystemConcepts;
import uk.nhs.fhir.makehtml.html.table.TableTitle;

public class CodeSystemConceptsTableDataProvider {

	private final FhirCodeSystemConcepts codeSystemConcepts;
	
	public CodeSystemConceptsTableDataProvider(FhirCodeSystemConcepts codeSystemConcepts) {
		this.codeSystemConcepts = codeSystemConcepts;
	}

	public List<TableTitle> getColumns() {
		
		int codePercent = 100;
		int descriptionPercent = 0;
		int definitionPercent = 0;

		List<CodeSystemConceptTableRowData> rows = getRows();
		
		if (includeDescriptionColumn(rows)) {
			if (codePercent > 25) {
				descriptionPercent = codePercent - 25;
				codePercent = 25;
			}
		}
		
		if (includeDefinitionColumn(rows)) {
			if (codePercent > 25) {
				definitionPercent = codePercent - 25;
				codePercent = 25;
			} else if (descriptionPercent > 25) {
				definitionPercent = descriptionPercent - 25;
				descriptionPercent = 25;
			}
		}
		
		List<TableTitle> columns = Lists.newArrayList();
		
		columns.add(new TableTitle("Code", "Code that identifies the concept.", Integer.toString(codePercent) + "%"));

		if (descriptionPercent > 0) {
			columns.add(new TableTitle("Description", "User-friendly name", Integer.toString(descriptionPercent) + "%"));
		}
		
		if (definitionPercent > 0) {
			columns.add(new TableTitle("Definition", "Formal definition.", Integer.toString(definitionPercent) + "%"));
		}
		
		return columns;
	}

	public List<CodeSystemConceptTableRowData> getRows() {
		List<CodeSystemConceptTableRowData> data = Lists.newArrayList();
		
		for (FhirCodeSystemConcept concept : codeSystemConcepts.getConcepts()) {
			String code = concept.getCode();
			Optional<String> description = concept.getDescription();
			Optional<String> definition = concept.getDefinition();

			data.add(new CodeSystemConceptTableRowData(code, description, definition));
		}
		
		return data;
	}
	
	public boolean includeDescriptionColumn() {
		return includeDescriptionColumn(getRows());
	}
	
	public boolean includeDescriptionColumn(List<CodeSystemConceptTableRowData> rows) {
		return rows.stream().anyMatch(row -> row.hasDescription());
	}
	
	public boolean includeDefinitionColumn() {
		return includeDefinitionColumn(getRows());
	}
	
	public boolean includeDefinitionColumn(List<CodeSystemConceptTableRowData> rows) {
		return rows.stream().anyMatch(row -> row.hasDefinition());
	}
}
