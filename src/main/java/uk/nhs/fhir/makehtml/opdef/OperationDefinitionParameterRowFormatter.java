package uk.nhs.fhir.makehtml.opdef;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.html.LinkCell;
import uk.nhs.fhir.makehtml.html.SimpleTextCell;
import uk.nhs.fhir.makehtml.html.TableRow;
import uk.nhs.fhir.makehtml.html.ValueWithInfoCell;

public class OperationDefinitionParameterRowFormatter {

	public TableRow formatRow(OperationDefinitionParameterTableData data) {
		return new TableRow(
			new SimpleTextCell(data.getRowTitle()),
			new SimpleTextCell(data.getCardinality()),
			new LinkCell(data.getTypeLink(), Lists.newArrayList(), Lists.newArrayList("fhir-link")),
			new ValueWithInfoCell(data.getValue(), data.getResourceFlags())
		);
	}
}
