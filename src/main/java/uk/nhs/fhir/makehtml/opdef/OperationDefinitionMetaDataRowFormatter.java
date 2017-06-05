package uk.nhs.fhir.makehtml.opdef;

import com.google.common.collect.Sets;

import uk.nhs.fhir.makehtml.html.FhirCSS;
import uk.nhs.fhir.makehtml.html.LinkCell;
import uk.nhs.fhir.makehtml.html.SimpleTextCell;
import uk.nhs.fhir.makehtml.html.TableRow;

public class OperationDefinitionMetaDataRowFormatter {
	public TableRow formatRow(OperationDefinitionMetaDataRowData source) {
		return new TableRow(
			new SimpleTextCell(source.getRowTitle()),
			new LinkCell(source.getTypeLink(), Sets.newHashSet(), Sets.newHashSet(FhirCSS.LINK)),
			new SimpleTextCell(source.getContent()));
	}
}
