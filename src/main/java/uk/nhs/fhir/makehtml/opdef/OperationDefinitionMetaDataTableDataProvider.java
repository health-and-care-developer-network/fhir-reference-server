package uk.nhs.fhir.makehtml.opdef;

import java.util.List;

import com.google.common.collect.Lists;

import ca.uhn.fhir.model.api.BasePrimitive;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.valueset.OperationKindEnum;
import ca.uhn.fhir.model.primitive.CodeDt;
import uk.nhs.fhir.makehtml.data.LinkData;
import uk.nhs.fhir.util.FhirDocLinkFactory;
import uk.nhs.fhir.util.StringUtil;
import uk.nhs.fhir.util.TableTitle;

public class OperationDefinitionMetaDataTableDataProvider {
	
	private final OperationDefinition source;
	private final FhirDocLinkFactory fhirDocLinkFactory;

	public OperationDefinitionMetaDataTableDataProvider(OperationDefinition source) {
		this(source, new FhirDocLinkFactory());
	}
	
	public OperationDefinitionMetaDataTableDataProvider(OperationDefinition source, FhirDocLinkFactory linkDataFactory) {
		this.source = source;
		this.fhirDocLinkFactory = linkDataFactory;
	}
	
	public List<TableTitle> getColumns() {
		return Lists.newArrayList(
			new TableTitle("Name", "The logical name of the element", "100px"),
			new TableTitle("Type", "Reference to the type of the element", "100px"),
			new TableTitle("Value", "Additional information about the element", "290px")
		);
	}

	public List<OperationDefinitionMetaDataRowData> getRows() {
		return Lists.newArrayList(
			createElementRow("Name", source.getNameElement()),
			createOperationKindRow("Kind", source.getKindElement().getValueAsEnum()),
			createElementRow("Description", source.getDescriptionElement()),
			createElementRow("Code", source.getCodeElement()),
			createElementRow("System", source.getSystemElement()),
			createElementRow("Instance", source.getInstanceElement())
		);
	}
	
	private OperationDefinitionMetaDataRowData createOperationKindRow(String desc, OperationKindEnum operationKind) {
		return new OperationDefinitionMetaDataRowData(
			desc, 
			new LinkData(operationKind.getSystem(), OperationKindEnum.VALUESET_NAME),
			StringUtil.capitaliseLowerCase(operationKind.getCode()));
	}

	private OperationDefinitionMetaDataRowData createElementRow(String desc, BasePrimitive<?> fhirData) {
		LinkData typeLink = fhirDocLinkFactory.forDataType(fhirData);
		
		String value = fhirData.getValueAsString();
		
		if (!(fhirData instanceof CodeDt)) {
			value = StringUtil.capitaliseLowerCase(value);
		}
		
		return new OperationDefinitionMetaDataRowData(desc, typeLink, value);
	}
}
