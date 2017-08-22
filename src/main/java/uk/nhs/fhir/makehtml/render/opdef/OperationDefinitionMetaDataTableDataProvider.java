package uk.nhs.fhir.makehtml.render.opdef;

import java.util.List;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.wrap.WrappedOperationDefinition;
import uk.nhs.fhir.makehtml.html.table.TableTitle;
import uk.nhs.fhir.util.StringUtil;

// KGM 8/May/2017 Altered meta table column widths

public class OperationDefinitionMetaDataTableDataProvider {
	
	private final WrappedOperationDefinition source;
	
	public OperationDefinitionMetaDataTableDataProvider(WrappedOperationDefinition source) {
		this.source = source;
	}
	
	public List<TableTitle> getColumns() {
		// KGM 8/May/2017 Altered meta table column to % widths
		return Lists.newArrayList(
			new TableTitle("Name", "The logical name of the element", "20%"),
			new TableTitle("Type", "Reference to the type of the element", "30%"),
			new TableTitle("Value", "Additional information about the element", "50%")
		);
	}

	public List<OperationDefinitionMetaDataRowData> getRows() {
		return Lists.newArrayList(
			new OperationDefinitionMetaDataRowData("Name", source.getNameTypeLink(), StringUtil.capitaliseLowerCase(source.getName())),
			new OperationDefinitionMetaDataRowData("Kind", source.getKindTypeLink(), StringUtil.capitaliseLowerCase(source.getKind())),
			new OperationDefinitionMetaDataRowData("Description", source.getDescriptionTypeLink(), StringUtil.capitaliseLowerCase(source.getDescription())),
			new OperationDefinitionMetaDataRowData("Code", source.getCodeTypeLink(), source.getCode()),
			new OperationDefinitionMetaDataRowData("System", source.getSystemTypeLink(), StringUtil.capitaliseLowerCase(source.getIsSystem())),
			new OperationDefinitionMetaDataRowData("Instance", source.getInstanceTypeLink(), StringUtil.capitaliseLowerCase(source.getIsInstance()))
		);
	}
}
