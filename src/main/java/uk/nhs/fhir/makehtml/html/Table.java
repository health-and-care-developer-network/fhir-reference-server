package uk.nhs.fhir.makehtml.html;

import java.util.List;
import java.util.Set;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.nhs.fhir.util.Elements;
import uk.nhs.fhir.util.TableTitle;

public class Table {
	private final List<TableTitle> cols = Lists.newArrayList();
	private final List<TableRow> rows = Lists.newArrayList();
	private final Set<String> tableClasses = Sets.newHashSet();
	
	public Table(List<TableTitle> cols, Set<String> tableClasses) {
		this(cols, Lists.newArrayList(), tableClasses);
	}
	
	public Table(List<TableTitle> cols, List<TableRow> rows, Set<String> tableClasses) {
		this.cols.addAll(cols);
		this.rows.addAll(rows);
		this.tableClasses.addAll(tableClasses);
	}
	
	public void addRow(TableRow row) {
		rows.add(row);
	}
	
	public Element makeTable() {
		List<Element> titleElements = Lists.newArrayList();
		cols.forEach((TableTitle col) -> titleElements.add(col.makeTitleCell()));
		
		List<Element> rowElements = Lists.newArrayList();
		rows.forEach((TableRow row) -> rowElements.add(row.makeRow()));
		
		return Elements.withAttributeAndChildren("table",
			new Attribute("class", "fhir-table"),
			Lists.newArrayList(
				Elements.withChild("thead",
					Elements.withAttributeAndChildren("tr", 
						new Attribute("class", "fhir-table-header-row"), 
						titleElements)),
				Elements.withChildren("tbody",
					rowElements)));
	}
	
	public List<TableRow> getRows() {
		return rows;
	}
}
