package uk.nhs.fhir.makehtml.render.opdef;

import java.util.List;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.ResourceInfo;
import uk.nhs.fhir.data.opdef.FhirOperationParameter;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.makehtml.html.table.TableTitle;

// KGM 8/May/2017 Altered meta table column to % widths

public class OperationDefinitionParameterTableDataProvider {

	private final List<FhirOperationParameter> parameters;
	
	public OperationDefinitionParameterTableDataProvider(List<FhirOperationParameter> parameters) {
		this.parameters = parameters;
	}

	public List<TableTitle> getColumns() {

		// KGM 8/May/2017 Altered meta table column to % widths
		return Lists.newArrayList(
			new TableTitle("Name", "The logical name of the element", "20%"),
			new TableTitle("Card.", "Minimum and maximum # of times the element can appear in the instance", "10%"),
			new TableTitle("Type", "Reference to the type of the element", "20%"),
			new TableTitle("Value", "Additional information about the element", "50%")
		);
	}

	public List<OperationDefinitionParameterTableData> getRows() {
		List<OperationDefinitionParameterTableData> data = Lists.newArrayList();
		
		for (FhirOperationParameter parameter : parameters) {
			String rowTitle = parameter.getName();
			String cardinality = parameter.getMin() + ".." + parameter.getMax(); 
			LinkDatas typeLink = new LinkDatas(parameter.getTypeLink());
			String documentation = parameter.getDocumentation();
			List<ResourceInfo> flags = parameter.getResourceInfos();

			data.add(new OperationDefinitionParameterTableData(rowTitle, cardinality, typeLink, documentation, flags));
		}
		
		return data;
	}
}
