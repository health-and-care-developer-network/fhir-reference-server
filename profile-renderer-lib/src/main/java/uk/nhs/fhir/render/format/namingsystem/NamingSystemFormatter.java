package uk.nhs.fhir.render.format.namingsystem;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.codesystem.FhirCodeSystemConcepts;
import uk.nhs.fhir.data.namingsystem.FhirNamingSystemUniqueId;
import uk.nhs.fhir.data.wrap.WrappedCodeSystem;
import uk.nhs.fhir.data.wrap.WrappedNamingSystem;
import uk.nhs.fhir.data.wrap.WrappedOperationDefinition;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.ResourceFormatter;
import uk.nhs.fhir.render.format.TableFormatter;
import uk.nhs.fhir.render.format.namingsystem.NamingSystemMetaDataRowData;
import uk.nhs.fhir.render.format.namingsystem.NamingSystemMetaDataRowFormatter;
import uk.nhs.fhir.render.format.namingsystem.NamingSystemMetaDataTableDataProvider;
import uk.nhs.fhir.render.format.namingsystem.NamingSystemIdentifierRowFormatter;
import uk.nhs.fhir.render.format.namingsystem.NamingSystemIdentifierTableData;
import uk.nhs.fhir.render.format.namingsystem.NamingSystemIdentifierTableDataProvider;
import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.table.Table;
import uk.nhs.fhir.render.html.table.TableRow;

public class NamingSystemFormatter extends ResourceFormatter<WrappedNamingSystem> {
	public NamingSystemFormatter(WrappedNamingSystem wrappedResource) {
		super(wrappedResource);
	}

	@Override
	public HTMLDocSection makeSectionHTML() {
		
		Element renderedNamingSystem =
				Elements.withAttributeAndChildren("div",
					new Attribute("id", "fhir-ref-operation-definition-structure"),
					Lists.newArrayList(
						buildMetaDataPanel(wrappedResource),
						buildIdentifierPanel("Identifier(s)", wrappedResource.getUniqueIds())
						));
			
			HTMLDocSection section = new HTMLDocSection();
			addStyles(section);
			section.addBodyElement(renderedNamingSystem);
			
			return section;
	}
	
	private Element buildMetaDataPanel(WrappedNamingSystem source) {
		NamingSystemMetaDataTableDataProvider tableData = new NamingSystemMetaDataTableDataProvider(source);
		List<NamingSystemMetaDataRowData> rows = tableData.getRows();
		NamingSystemMetaDataRowFormatter rowFormatter = new NamingSystemMetaDataRowFormatter();
		List<TableRow> tableRows = Lists.newArrayList();
		rows.forEach(data -> tableRows.add(rowFormatter.formatRow(data)));
		
		Element metaDataTable = new Table(tableData.getColumns(), tableRows).makeTable();
		return new FhirPanel("Meta", metaDataTable).makePanel();
	}
	
	private Element buildIdentifierPanel(String panelTitle, List<FhirNamingSystemUniqueId> parameters) {
		NamingSystemIdentifierTableDataProvider tableData = new NamingSystemIdentifierTableDataProvider(parameters);
		List<NamingSystemIdentifierTableData> rows = tableData.getRows();
		NamingSystemIdentifierRowFormatter rowFormatter = new NamingSystemIdentifierRowFormatter();
		List<TableRow> tableRows = Lists.newArrayList();
		rows.forEach(data -> tableRows.add(rowFormatter.formatRow(data)));
		
		Element parametersTable = new Table(tableData.getColumns(), tableRows).makeTable();
		return new FhirPanel(panelTitle, parametersTable).makePanel();
	}
	
	private void addStyles(HTMLDocSection section) {
		//section.addStyles(getStyles());
		section.addStyles(FhirPanel.getStyles());
		section.addStyles(Table.getStyles());
	}
	
}