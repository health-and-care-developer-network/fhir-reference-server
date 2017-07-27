package uk.nhs.fhir.makehtml.render.opdef;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.nhs.fhir.makehtml.data.FhirOperationParameter;
import uk.nhs.fhir.makehtml.data.ResourceSectionType;
import uk.nhs.fhir.makehtml.data.wrap.WrappedOperationDefinition;
import uk.nhs.fhir.makehtml.html.FhirPanel;
import uk.nhs.fhir.makehtml.html.LinkCell;
import uk.nhs.fhir.makehtml.html.Table;
import uk.nhs.fhir.makehtml.html.TableRow;
import uk.nhs.fhir.makehtml.html.ValueWithInfoCell;
import uk.nhs.fhir.makehtml.html.jdom2.Elements;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;

public class OperationDefinitionFormatter extends ResourceFormatter<WrappedOperationDefinition> {

	public OperationDefinitionFormatter(WrappedOperationDefinition wrappedResource) {
		super(wrappedResource);
		this.resourceSectionType = ResourceSectionType.TREEVIEW;  
	}

	@Override
	public HTMLDocSection makeSectionHTML(WrappedOperationDefinition operationDefinition) throws ParserConfigurationException {
		Element renderedOperationDefinition =
			Elements.withAttributeAndChildren("div",
				new Attribute("id", "fhir-ref-operation-definition-structure"),
				Lists.newArrayList(
					buildMetaDataPanel(operationDefinition),
					buildParameterPanel("Input Parameters", operationDefinition.getInputParameters()),
					buildParameterPanel("Output Parameters", operationDefinition.getOutputParameters())));
		
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
		rows.forEach((OperationDefinitionMetaDataRowData data) -> tableRows.add(rowFormatter.formatRow(data)));
		
		Element metaDataTable = new Table(tableData.getColumns(), tableRows, Sets.newHashSet()).makeTable();
		return new FhirPanel("Meta", metaDataTable).makePanel();
	}

	private Element buildParameterPanel(String panelTitle, List<FhirOperationParameter> parameters) {
		OperationDefinitionParameterTableDataProvider tableData = new OperationDefinitionParameterTableDataProvider(parameters);
		List<OperationDefinitionParameterTableData> rows = tableData.getRows();
		OperationDefinitionParameterRowFormatter rowFormatter = new OperationDefinitionParameterRowFormatter();
		List<TableRow> tableRows = Lists.newArrayList();
		rows.forEach((OperationDefinitionParameterTableData data) -> tableRows.add(rowFormatter.formatRow(data)));
		
		Element parametersTable = new Table(tableData.getColumns(), tableRows, Sets.newHashSet()).makeTable();
		return new FhirPanel(panelTitle, parametersTable).makePanel();
	}

	private void addStyles(HTMLDocSection section) {
		Table.getStyles().forEach(section::addStyle);
		FhirPanel.getStyles().forEach(section::addStyle);
		ValueWithInfoCell.getStyles().forEach(section::addStyle);
		LinkCell.getStyles().forEach(section::addStyle);
	}
}
