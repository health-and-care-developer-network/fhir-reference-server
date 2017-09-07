package uk.nhs.fhir.makehtml.render.opdef;

import com.google.common.collect.Sets;

import uk.nhs.fhir.makehtml.html.cell.LinkCell;
import uk.nhs.fhir.makehtml.html.cell.SimpleTextCell;
import uk.nhs.fhir.makehtml.html.cell.ValueWithInfoCell;
import uk.nhs.fhir.makehtml.html.style.FhirCSS;
import uk.nhs.fhir.makehtml.html.table.TableRow;

public class OperationDefinitionParameterRowFormatter {

	public TableRow formatRow(OperationDefinitionParameterTableData data) {
		return new TableRow(
			new SimpleTextCell(data.getRowTitle()),
			new SimpleTextCell(data.getCardinality()),
			new LinkCell(data.getTypeLink(), Sets.newHashSet(), Sets.newHashSet(FhirCSS.LINK)),
			new ValueWithInfoCell(data.getValue(), data.getResourceFlags())
		);
	}
}
