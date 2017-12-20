package uk.nhs.fhir.makehtml.render.codesys;

import java.util.List;

import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.codesystem.FhirCodeSystemFilter;
import uk.nhs.fhir.data.wrap.WrappedCodeSystem;
import uk.nhs.fhir.makehtml.html.panel.FhirPanel;
import uk.nhs.fhir.makehtml.html.table.Table;
import uk.nhs.fhir.makehtml.html.table.TableFormatter;
import uk.nhs.fhir.makehtml.html.table.TableRow;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;

public class CodeSystemFiltersTableFormatter extends TableFormatter<WrappedCodeSystem> {

	public CodeSystemFiltersTableFormatter(WrappedCodeSystem wrappedResource) {
		super(wrappedResource);
	}

	@Override
	public HTMLDocSection makeSectionHTML() {
		
		HTMLDocSection section = new HTMLDocSection();
		addStyles(section);
		
		List<FhirCodeSystemFilter> filters = wrappedResource.getFilters();
		
		if (filters.isEmpty()) {
			return null;
		} else {
			Element filtersPanel = buildFiltersPanel(filters);
			section.addBodyElement(filtersPanel);
		}

		return section;
	}

	private Element buildFiltersPanel(List<FhirCodeSystemFilter> filters) {
		CodeSystemFiltersTableDataProvider tableData = new CodeSystemFiltersTableDataProvider(filters);
		
		List<CodeSystemFilterTableRowData> rows = tableData.getRows();
		CodeSystemFilterRowFormatter rowFormatter = new CodeSystemFilterRowFormatter();
		
		List<TableRow> tableRows = Lists.newArrayList();
		rows.forEach(data -> tableRows.add(rowFormatter.formatRow(data)));
		
		Element filtersTable = new Table(tableData.getColumns(), tableRows).makeTable();
		return new FhirPanel("Available Filters", filtersTable).makePanel();
	}

	private void addStyles(HTMLDocSection section) {
		section.addStyles(FhirPanel.getStyles());
		section.addStyles(Table.getStyles());
		
	}

}
