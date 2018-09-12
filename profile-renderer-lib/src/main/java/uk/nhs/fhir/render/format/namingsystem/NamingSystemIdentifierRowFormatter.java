package uk.nhs.fhir.render.format.namingsystem;

import uk.nhs.fhir.render.html.cell.SimpleTextCell;
import uk.nhs.fhir.render.html.table.TableRow;

public class NamingSystemIdentifierRowFormatter {
	
	public TableRow formatRow(NamingSystemIdentifierTableData data) {
		return new TableRow(
			new SimpleTextCell(data.getRowValue()),
			new SimpleTextCell(data.getType()),
			new SimpleTextCell(String.valueOf(data.getPreferred()))
			
		);
	}

}

	