package uk.nhs.fhir.makehtml.render.codesys;

import java.util.List;

import uk.nhs.fhir.makehtml.html.cell.SimpleTextCell;
import uk.nhs.fhir.makehtml.html.table.TableFormatter;
import uk.nhs.fhir.makehtml.html.table.TableRow;

public class CodeSystemConceptRowFormatter {
	private final CodeSystemConceptsTableDataProvider tableData;
	private final List<CodeSystemConceptTableRowData> rows;
	
	public CodeSystemConceptRowFormatter(CodeSystemConceptsTableDataProvider tableData, List<CodeSystemConceptTableRowData> rows) {
		this.tableData = tableData;
		this.rows = rows;
	}

	public TableRow formatRow(CodeSystemConceptTableRowData data) {
		TableRow row = new TableRow();
		
		row.addCell(new SimpleTextCell(data.getCode(), true));
		
		if (tableData.includeDescriptionColumn(rows)) {
			row.addCell(new SimpleTextCell(data.getDescription().orElse(TableFormatter.BLANK), true));
		}
		
		if (tableData.includeDefinitionColumn(rows)) {
			row.addCell(new SimpleTextCell(data.getDefinition().orElse(TableFormatter.BLANK), true));
		}
		
		return row;
	}
}
