package uk.nhs.fhir.makehtml.opdef;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.fmt.LinkCell;
import uk.nhs.fhir.makehtml.fmt.SimpleTextCell;
import uk.nhs.fhir.makehtml.fmt.TableRow;
import uk.nhs.fhir.makehtml.fmt.ValueWithResourceFlagsCell;

public class OperationDefinitionParameterRowFormatter {

	public TableRow formatRow(OperationDefinitionParameterTableData data) {
		return new TableRow(
			new SimpleTextCell(data.getRowTitle()),
			new SimpleTextCell(data.getCardinality()),
			new LinkCell(data.getTypeLink(), Lists.newArrayList(), Lists.newArrayList("fhir-link")),
			new ValueWithResourceFlagsCell(data.getValue(), data.getResourceFlags())
		);
	}
}
