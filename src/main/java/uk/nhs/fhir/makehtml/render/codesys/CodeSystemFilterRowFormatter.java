package uk.nhs.fhir.makehtml.render.codesys;

import uk.nhs.fhir.makehtml.html.cell.SimpleTextCell;
import uk.nhs.fhir.makehtml.html.table.TableRow;

public class CodeSystemFilterRowFormatter {
	public TableRow formatRow(CodeSystemFilterTableRowData data) {
		TableRow row = new TableRow(
			new SimpleTextCell(data.getCode(), true),
			new SimpleTextCell(data.getOperators(), true),
			new SimpleTextCell(data.getValue(), true));
		
		if (data.hasDocumentation()) {
			row.addCell(new SimpleTextCell(data.getDocumentation().get(), true));
		}
		
		return row;
	}
}
