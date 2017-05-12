package uk.nhs.fhir.makehtml.opdef;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition.Parameter;
import uk.nhs.fhir.makehtml.HTMLDocSection;
import uk.nhs.fhir.makehtml.ResourceFormatter;
import uk.nhs.fhir.makehtml.data.ResourceSectionType;
import uk.nhs.fhir.makehtml.html.FhirPanel;
import uk.nhs.fhir.makehtml.html.LinkCell;
import uk.nhs.fhir.makehtml.html.Table;
import uk.nhs.fhir.makehtml.html.TableRow;
import uk.nhs.fhir.makehtml.html.ValueWithInfoCell;
import uk.nhs.fhir.util.Elements;
import uk.nhs.fhir.util.FhirDocLinkFactory;

public class OperationDefinitionFormatter extends ResourceFormatter {

	public OperationDefinitionFormatter() { this.resourceSectionType = ResourceSectionType.TREEVIEW;  }

	@Override
	public HTMLDocSection makeSectionHTML(IBaseResource source) throws ParserConfigurationException {
		OperationDefinition operationDefinition = (OperationDefinition)source;
		
		List<Parameter> inputParameters = Lists.newArrayList();
		List<Parameter> outputParameters = Lists.newArrayList();
		populateParameters(operationDefinition, inputParameters, outputParameters);
		
		Element renderedOperationDefinition =
			Elements.withAttributeAndChildren("div",
				new Attribute("id", "fhir-ref-operation-definition-structure"),
				Lists.newArrayList(
					buildMetaDataPanel(operationDefinition),
					buildParameterPanel("Input Parameters", inputParameters, fhirDocLinkFactory),
					buildParameterPanel("Output Parameters", outputParameters, fhirDocLinkFactory)));
		
		HTMLDocSection section = new HTMLDocSection();
		addStyles(section);
		section.addBodyElement(renderedOperationDefinition);
		
		return section;
	}

	private void populateParameters(OperationDefinition source, List<Parameter> inputParameters,
			List<Parameter> outputParameters) {
		for (Parameter parameter : source.getParameter()) {
			switch (parameter.getUseElement().getValueAsEnum()) {
			case IN:
				inputParameters.add(parameter);
				break;
			case OUT:
				outputParameters.add(parameter);
				break;
			}
		}
	}

	private Element buildMetaDataPanel(OperationDefinition source) {
		OperationDefinitionMetaDataTableDataProvider tableData = new OperationDefinitionMetaDataTableDataProvider(source);
		List<OperationDefinitionMetaDataRowData> rows = tableData.getRows();
		OperationDefinitionMetaDataRowFormatter rowFormatter = new OperationDefinitionMetaDataRowFormatter();
		List<TableRow> tableRows = Lists.newArrayList();
		rows.forEach((OperationDefinitionMetaDataRowData data) -> tableRows.add(rowFormatter.formatRow(data)));
		
		Element metaDataTable = new Table(tableData.getColumns(), tableRows, Sets.newHashSet()).makeTable();
		return new FhirPanel("Meta", metaDataTable).makePanel();
	}

	private Element buildParameterPanel(String panelTitle, List<Parameter> parameters, FhirDocLinkFactory fhirDocLinkFactory) {
		OperationDefinitionParameterTableDataProvider tableData = new OperationDefinitionParameterTableDataProvider(parameters, fhirDocLinkFactory);
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
