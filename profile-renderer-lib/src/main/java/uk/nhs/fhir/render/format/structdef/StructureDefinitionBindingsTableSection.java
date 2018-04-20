package uk.nhs.fhir.render.format.structdef;

import java.util.List;
import java.util.Optional;

import uk.nhs.fhir.data.structdef.tree.SnapshotTreeNode;

public class StructureDefinitionBindingsTableSection {
	private final Optional<SnapshotTreeNode> sourceNode;
	private final List<StructureDefinitionBindingsTableRowData> rowData;
	
	public StructureDefinitionBindingsTableSection(List<StructureDefinitionBindingsTableRowData> rowData) {
		this.sourceNode = Optional.empty();
		this.rowData = rowData;
	}
	
	public StructureDefinitionBindingsTableSection(SnapshotTreeNode sourceNode,
			List<StructureDefinitionBindingsTableRowData> rowData) {
		this.sourceNode = Optional.of(sourceNode);
		this.rowData = rowData;
	}

	public Optional<SnapshotTreeNode> getSourceNode() {
		return sourceNode;
	}
	
	public List<StructureDefinitionBindingsTableRowData> getRowData() {
		return rowData;
	}
}
