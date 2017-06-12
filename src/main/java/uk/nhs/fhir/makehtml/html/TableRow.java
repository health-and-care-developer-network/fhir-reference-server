package uk.nhs.fhir.makehtml.html;

import java.util.Arrays;
import java.util.List;

import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.html.jdom2.Elements;

public class TableRow {
	private List<TableCell> tableCells = Lists.newArrayList();

	public TableRow(TableCell cell) {
		addCell(cell);
	}
	public TableRow(List<TableCell> cells) {
		addCells(cells);
	}
	public TableRow(TableCell... cells) {
		addCells(Arrays.asList(cells));
	}
	
	public void addCell(TableCell cell) {
		tableCells.add(cell);
	}
	
	public void addCells(List<TableCell> cells) {
		tableCells.addAll(cells);
	}
	
	public List<TableCell> getCells() {
		return tableCells;
	}
	
	public Element makeRow() {
		List<Element> cells = Lists.newArrayList();
		tableCells.forEach((TableCell cell) -> cells.add(cell.makeCell()));
		return Elements.withChildren("tr", cells);
	}
}
