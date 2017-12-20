package uk.nhs.fhir.makehtml.render.valueset;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.wrap.WrappedValueSet;
import uk.nhs.fhir.makehtml.html.cell.LinkCell;
import uk.nhs.fhir.makehtml.html.panel.FhirPanel;
import uk.nhs.fhir.makehtml.html.table.Table;
import uk.nhs.fhir.makehtml.html.table.TableFormatter;
import uk.nhs.fhir.makehtml.html.table.TableRow;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;

public class FilteredValueSetTableFormatter extends TableFormatter<WrappedValueSet> {

	public FilteredValueSetTableFormatter(WrappedValueSet wrappedResource) {
		super(wrappedResource);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		HTMLDocSection section = new HTMLDocSection();
		addStyles(section);
		
		Element filteredCodeSystemPanel = buildFilteredCodeSystemPanel();
		section.addBodyElement(filteredCodeSystemPanel);
		
		return section;
	}
	
	private Element buildFilteredCodeSystemPanel() {
		ValueSetFilteredCodeSystemTableDataProvider tableData = new ValueSetFilteredCodeSystemTableDataProvider(wrappedResource);
		List<ValueSetFilteredCodeSystemTableData> rows = tableData.getRows();
		ValueSetFilteredCodeSystemRowFormatter rowFormatter = new ValueSetFilteredCodeSystemRowFormatter();
		List<TableRow> tableRows = Lists.newArrayList();
		rows.forEach(data -> tableRows.add(rowFormatter.formatRow(data)));
		
		Element filteredCodeSystemTable = new Table(tableData.getColumns(), tableRows).makeTable();
		return new FhirPanel("Filtered code system", filteredCodeSystemTable).makePanel();
	}
	
	public void addStyles(HTMLDocSection section) {
		section.addStyles(TableFormatter.getStyles());
		section.addStyles(LinkCell.getStyles());
		section.addStyles(FhirPanel.getStyles());
		section.addStyles(Table.getStyles());
	}
}
