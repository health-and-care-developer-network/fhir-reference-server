package uk.nhs.fhir.render.format.namingsystem;


import com.google.common.collect.Sets;

import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.render.format.namingsystem.NamingSystemMetaDataRowData;
import uk.nhs.fhir.render.html.cell.LinkCell;
import uk.nhs.fhir.render.html.cell.SimpleTextCell;
import uk.nhs.fhir.render.html.style.FhirCSS;
import uk.nhs.fhir.render.html.table.TableRow;

public class NamingSystemMetaDataRowFormatter {
	
	public TableRow formatRow(NamingSystemMetaDataRowData source) {
		return new TableRow(
			new SimpleTextCell(source.getRowTitle()),
			new SimpleTextCell(source.getContent()));
	}

}