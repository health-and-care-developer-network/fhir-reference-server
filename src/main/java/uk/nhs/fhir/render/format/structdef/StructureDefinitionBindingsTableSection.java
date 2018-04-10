package uk.nhs.fhir.render.format.structdef;

import java.util.List;
import java.util.Optional;

public class StructureDefinitionBindingsTableSection {
	private final Optional<String> sourceNodeKey;
	private final Optional<String> sourceExternalResourceUrl;
	private final List<StructureDefinitionBindingsTableRowData> rowData;
	
	public StructureDefinitionBindingsTableSection(List<StructureDefinitionBindingsTableRowData> rowData) {
		this.sourceNodeKey = Optional.empty();
		this.sourceExternalResourceUrl = Optional.empty();
		this.rowData = rowData;
	}
	
	public StructureDefinitionBindingsTableSection(String sourceNodeKey, String sourceExternalResourceUrl, List<StructureDefinitionBindingsTableRowData> rowData) {
		this.sourceNodeKey = Optional.of(sourceNodeKey);
		this.sourceExternalResourceUrl = Optional.of(sourceExternalResourceUrl);
		this.rowData = rowData;
	}
	
	public Optional<String> getSourceNodeKey() {
		return sourceNodeKey;
	}
	
	public Optional<String> getSourceExternalResourceUrl() {
		return sourceExternalResourceUrl;
	}
	
	public List<StructureDefinitionBindingsTableRowData> getRowData() {
		return rowData;
	}
}
