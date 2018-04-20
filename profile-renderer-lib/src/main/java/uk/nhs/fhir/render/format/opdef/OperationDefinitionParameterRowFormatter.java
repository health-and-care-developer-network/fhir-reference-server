package uk.nhs.fhir.render.format.opdef;

import com.google.common.collect.Sets;

import uk.nhs.fhir.render.html.cell.LinkCell;
import uk.nhs.fhir.render.html.cell.SimpleTextCell;
import uk.nhs.fhir.render.html.cell.ValueWithInfoCell;
import uk.nhs.fhir.render.html.style.FhirCSS;
import uk.nhs.fhir.render.html.table.TableRow;

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
