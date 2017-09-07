package uk.nhs.fhir.makehtml.render.valueset;

import java.util.List;
import java.util.Optional;

import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.LinkData;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.makehtml.html.cell.LinkCell;
import uk.nhs.fhir.makehtml.html.cell.SimpleTextCell;
import uk.nhs.fhir.makehtml.html.cell.TableCell;
import uk.nhs.fhir.makehtml.html.table.TableRow;

public class ValueSetConceptsCodeSystemFormatter {

	private final boolean needsDisplayColumn;
	private final boolean needsDefinitionColumn;
	private final boolean needsMappingColumn;
	private final List<TableRow> tableRows;

	public ValueSetConceptsCodeSystemFormatter(boolean needsDisplayColumn, boolean needsDefinitionColumn, boolean needsMappingColumn, List<TableRow> tableRows) {
		this.needsDisplayColumn = needsDisplayColumn;
		this.needsDefinitionColumn = needsDefinitionColumn;
		this.needsMappingColumn = needsMappingColumn;
		this.tableRows = tableRows;
	}

	public void addRows(ValueSetConceptsTableDataCodeSystem codeSystem) {
		addHeaderRow(codeSystem.getCodeSystem());
		
		for (ValueSetConceptsTableData concept : codeSystem.getConcepts()) {
			addConceptRow(concept);
		}
	}

	private void addConceptRow(ValueSetConceptsTableData concept) {
		String code = concept.getCode();
		Optional<String> display = concept.getDisplay();
		Optional<String> definition = concept.getDefinition();
		Optional<String> mapping = concept.getMapping();
		
		TableRow row = new TableRow(
			TableCell.empty(),
			new SimpleTextCell(code, true));

		if (needsDisplayColumn) {
			row.addCell(new SimpleTextCell(display.orElse(TableCell.ZERO_WIDTH_CHARACTER), true));
		}
		
		if (needsDefinitionColumn) {
			row.addCell(new SimpleTextCell(definition.orElse(TableCell.ZERO_WIDTH_CHARACTER), true));		
		}
		
		if (needsMappingColumn) {
			row.addCell(new SimpleTextCell(mapping.orElse(TableCell.ZERO_WIDTH_CHARACTER), true));
		}
		
		tableRows.add(row);
	}

	void addHeaderRow(FhirURL codeSystem) {
		TableCell codeSystemCell;
		if (codeSystem.isLogicalUrl()) {
			codeSystemCell = new SimpleTextCell(codeSystem.toFullString(), true);
		} else {
			codeSystemCell = new LinkCell(new LinkDatas(new LinkData(codeSystem, codeSystem.toFullString())), true);
		} 
		
		TableRow row = new TableRow(codeSystemCell,	TableCell.empty(), TableCell.empty());
		
		if (needsDisplayColumn) {
			row.addCell(TableCell.empty());
		}
		if (needsDefinitionColumn) {
			row.addCell(TableCell.empty());
		}
		if (needsMappingColumn) {
			row.addCell(TableCell.empty());
		}
			
		tableRows.add(row);
	}
	
}
