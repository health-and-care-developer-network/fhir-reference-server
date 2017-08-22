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

		List<CodeSystemConceptTableRowData> rows = getRows();
		boolean includeDescriptionColumn = includeDescriptionColumn(rows);
		boolean includeDefinitionColumn = includeDefinitionColumn(rows);
		
		int columnsCount = 1;
		if (includeDescriptionColumn) {
			columnsCount++;
		}
		if (includeDefinitionColumn) {
			columnsCount++;
		}
		
		List<TableTitle> columns = Lists.newArrayList();
		
		String codeWidth;
		String definitionWidth;
		String descriptionWidth;
		switch(columnsCount) {
		case 1:
			codeWidth = "100%";
			definitionWidth = "0%";
			descriptionWidth = "0%";
			break;
		case 2:
			codeWidth = "20%";
			definitionWidth = "80%";
			descriptionWidth = "80%";
			break;
		case 3:
			codeWidth = "20%";
			definitionWidth = "40%";
			descriptionWidth = "40%";
			break;
		default:
			throw new IllegalStateException("Didn't expect " + columnsCount + " columns");
		}
		
		columns.add(new TableTitle("Code", "Code that identifies the concept.", codeWidth));

		if (includeDefinitionColumn) {
			columns.add(new TableTitle("Description", "User-friendly name", descriptionWidth));
		}
		
		if (includeDefinitionColumn) {
			columns.add(new TableTitle("Definition", "Formal definition.", definitionWidth));
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
