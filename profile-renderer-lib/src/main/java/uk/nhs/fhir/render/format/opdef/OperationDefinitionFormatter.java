package uk.nhs.fhir.render.format.opdef;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.opdef.FhirOperationParameter;
import uk.nhs.fhir.data.wrap.WrappedOperationDefinition;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.ResourceFormatter;
import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.cell.LinkCell;
import uk.nhs.fhir.render.html.cell.ValueWithInfoCell;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.table.Table;
import uk.nhs.fhir.render.html.table.TableRow;

public class OperationDefinitionFormatter extends ResourceFormatter<WrappedOperationDefinition> {

	public OperationDefinitionFormatter(WrappedOperationDefinition wrappedResource) {
		super(wrappedResource);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		Element renderedOperationDefinition =
			Elements.withAttributeAndChildren("div",
				new Attribute("id", "fhir-ref-operation-definition-structure"),
				Lists.newArrayList(
					buildMetaDataPanel(wrappedResource),
					buildParameterPanel("Input Parameters", wrappedResource.getInputParameters()),
					buildParameterPanel("Output Parameters", wrappedResource.getOutputParameters())));
		
		HTMLDocSection section = new HTMLDocSection();
		addStyles(section);
		section.addBodyElement(renderedOperationDefinition);
		
		return section;
	}

	private Element buildMetaDataPanel(WrappedOperationDefinition source) {
		OperationDefinitionMetaDataTableDataProvider tableData = new OperationDefinitionMetaDataTableDataProvider(source);
		List<OperationDefinitionMetaDataRowData> rows = tableData.getRows();
		OperationDefinitionMetaDataRowFormatter rowFormatter = new OperationDefinitionMetaDataRowFormatter();
		List<TableRow> tableRows = Lists.newArrayList();
		rows.forEach(data -> tableRows.add(rowFormatter.formatRow(data)));
		
		Element metaDataTable = new Table(tableData.getColumns(), tableRows).makeTable();
		return new FhirPanel("Meta", metaDataTable).makePanel();
	}

	private Element buildParameterPanel(String panelTitle, List<FhirOperationParameter> parameters) {
		OperationDefinitionParameterTableDataProvider tableData = new OperationDefinitionParameterTableDataProvider(parameters);
		List<OperationDefinitionParameterTableData> rows = tableData.getRows();
		OperationDefinitionParameterRowFormatter rowFormatter = new OperationDefinitionParameterRowFormatter();
		List<TableRow> tableRows = Lists.newArrayList();
		rows.forEach(data -> tableRows.add(rowFormatter.formatRow(data)));
		
		Element parametersTable = new Table(tableData.getColumns(), tableRows).makeTable();
		return new FhirPanel(panelTitle, parametersTable).makePanel();
	}

	private void addStyles(HTMLDocSection section) {
		Table.getStyles().forEach(section::addStyle);
		FhirPanel.getStyles().forEach(section::addStyle);
		ValueWithInfoCell.getStyles().forEach(section::addStyle);
		LinkCell.getStyles().forEach(section::addStyle);
	}
}
