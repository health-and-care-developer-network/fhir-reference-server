package uk.nhs.fhir.render.format.opdef;

import com.google.common.collect.Sets;

import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.render.html.cell.LinkCell;
import uk.nhs.fhir.render.html.cell.SimpleTextCell;
import uk.nhs.fhir.render.html.style.FhirCSS;
import uk.nhs.fhir.render.html.table.TableRow;

public class OperationDefinitionMetaDataRowFormatter {
	public TableRow formatRow(OperationDefinitionMetaDataRowData source) {
		return new TableRow(
			new SimpleTextCell(source.getRowTitle()),
			new LinkCell(new LinkDatas(source.getTypeLink()), Sets.newHashSet(), Sets.newHashSet(FhirCSS.LINK)),
			new SimpleTextCell(source.getContent()));
	}
}
