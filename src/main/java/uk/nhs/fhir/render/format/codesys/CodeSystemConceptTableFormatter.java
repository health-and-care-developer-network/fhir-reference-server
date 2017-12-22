package uk.nhs.fhir.render.format.codesys;

import java.util.List;

import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.codesystem.FhirCodeSystemConcepts;
import uk.nhs.fhir.data.wrap.WrappedCodeSystem;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.TableFormatter;
import uk.nhs.fhir.render.html.jdom2.Elements;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.table.Table;
import uk.nhs.fhir.render.html.table.TableRow;

public class CodeSystemConceptTableFormatter extends TableFormatter<WrappedCodeSystem> {

	public CodeSystemConceptTableFormatter(WrappedCodeSystem wrappedResource) {
		super(wrappedResource);
	}

	@Override
	public HTMLDocSection makeSectionHTML() {
		
		HTMLDocSection section = new HTMLDocSection();
		addStyles(section);
		
		FhirCodeSystemConcepts codeSystemConcepts = wrappedResource.getCodeSystemConcepts();
		
		if (codeSystemConcepts.getConcepts().isEmpty()) {
			return null;
		} else {
			Element conceptsPanel = buildConceptsPanel(codeSystemConcepts);
			section.addBodyElement(conceptsPanel);
		}

		return section;
	}

	private Element buildConceptsPanel(FhirCodeSystemConcepts codeSystemConcepts) {
		CodeSystemConceptsTableDataProvider tableData = new CodeSystemConceptsTableDataProvider(codeSystemConcepts);
		
		List<CodeSystemConceptTableRowData> rows = tableData.getRows();
		CodeSystemConceptRowFormatter rowFormatter = new CodeSystemConceptRowFormatter(tableData, rows);
		
		List<TableRow> tableRows = Lists.newArrayList();
		rows.forEach(data -> tableRows.add(rowFormatter.formatRow(data)));
		
		Element wrapperDiv = 
			Elements.withChildren("div", 
				Elements.withText("div", "System: " + codeSystemConcepts.getSystem()),
				Elements.newElement("br"),
				new Table(tableData.getColumns(), tableRows).makeTable());
		return new FhirPanel("Codes defined by " + wrappedResource.getUserFriendlyName(), wrapperDiv).makePanel();
	}

	private void addStyles(HTMLDocSection section) {
		section.addStyles(getStyles());
		section.addStyles(FhirPanel.getStyles());
		section.addStyles(Table.getStyles());
	}
}
