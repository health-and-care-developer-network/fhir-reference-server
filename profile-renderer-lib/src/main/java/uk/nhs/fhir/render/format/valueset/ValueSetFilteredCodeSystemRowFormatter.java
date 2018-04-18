package uk.nhs.fhir.render.format.valueset;

import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.LinkData;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.render.html.cell.LinkCell;
import uk.nhs.fhir.render.html.cell.SimpleTextCell;
import uk.nhs.fhir.render.html.cell.TableCell;
import uk.nhs.fhir.render.html.table.TableRow;

public class ValueSetFilteredCodeSystemRowFormatter {

	public TableRow formatRow(ValueSetFilteredCodeSystemTableData data) {
		FhirURL codeSystem = data.getCodeSystem();
		TableCell codeSystemCell = codeSystem.isLogicalUrl() ?
			new SimpleTextCell(codeSystem.toFullString(), true) :
			new LinkCell(new LinkDatas(new LinkData(codeSystem, codeSystem.toFullString())), true);
		
		TableRow row = new TableRow(
			codeSystemCell,
			new SimpleTextCell(data.getConcept(), true),
			new SimpleTextCell(data.getOperation(), true),
			new SimpleTextCell(data.getValue(), true));
		
		return row;
	}

}
