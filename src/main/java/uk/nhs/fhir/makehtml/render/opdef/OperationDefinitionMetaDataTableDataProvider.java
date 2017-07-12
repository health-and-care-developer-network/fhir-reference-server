package uk.nhs.fhir.makehtml.render.opdef;

import java.util.List;

import com.google.common.collect.Lists;

import ca.uhn.fhir.model.api.BasePrimitive;
import ca.uhn.fhir.model.dstu2.valueset.OperationKindEnum;
import ca.uhn.fhir.model.primitive.CodeDt;
import uk.nhs.fhir.makehtml.data.FhirURL;
import uk.nhs.fhir.makehtml.data.LinkData;
import uk.nhs.fhir.makehtml.data.SimpleLinkData;
import uk.nhs.fhir.makehtml.data.wrap.WrappedOperationDefinition;
import uk.nhs.fhir.makehtml.html.TableTitle;
import uk.nhs.fhir.util.FhirDocLinkFactory;
import uk.nhs.fhir.util.StringUtil;

// KGM 8/May/2017 Altered meta table column widths

public class OperationDefinitionMetaDataTableDataProvider {
	
	private final WrappedOperationDefinition source;
	private final FhirDocLinkFactory fhirDocLinkFactory;

	public OperationDefinitionMetaDataTableDataProvider(WrappedOperationDefinition source) {
		this(source, new FhirDocLinkFactory());
	}
	
	public OperationDefinitionMetaDataTableDataProvider(WrappedOperationDefinition source, FhirDocLinkFactory linkDataFactory) {
		this.source = source;
		this.fhirDocLinkFactory = linkDataFactory;
	}
	
	public List<TableTitle> getColumns() {
		// KGM 8/May/2017 Altered meta table column to % widths
		return Lists.newArrayList(
			new TableTitle("Name", "The logical name of the element", "25%"),
			new TableTitle("Type", "Reference to the type of the element", "25%"),
			new TableTitle("Value", "Additional information about the element", "50%")
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
			new SimpleLinkData(FhirURL.buildOrThrow(operationKind.getSystem()), OperationKindEnum.VALUESET_NAME),
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
