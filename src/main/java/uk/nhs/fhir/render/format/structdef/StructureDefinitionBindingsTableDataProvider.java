package uk.nhs.fhir.render.format.structdef;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.structdef.BindingInfo;
import uk.nhs.fhir.data.structdef.tree.SnapshotData;
import uk.nhs.fhir.data.structdef.tree.SnapshotTreeNode;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.render.html.table.TableTitle;

public class StructureDefinitionBindingsTableDataProvider {
	private final List<SnapshotTreeNode> nodesWithBindings;
	
	public StructureDefinitionBindingsTableDataProvider(List<SnapshotTreeNode> nodesWithBindings) {
		this.nodesWithBindings = nodesWithBindings;
	}
	
	public List<TableTitle> getColumns() {
		return Lists.newArrayList(
			new TableTitle("Path", "Identifies the bound node within the StructureDefinition", "20%"),
			new TableTitle("Definition", "Node description", "40%"),
			new TableTitle("Type", "Type of binding.", "10%"),
			new TableTitle("Reference", "ValueSet containing permitted codes.", "30%"));
	}

	public List<StructureDefinitionBindingsTableRowData> getRows() {
		return nodesWithBindings
			.stream()
			.map(node -> toRowData(node))
			.collect(Collectors.toList());
	}
	
	private StructureDefinitionBindingsTableRowData toRowData(SnapshotTreeNode node) {
		SnapshotData nodeData = node.getData();
		BindingInfo bindingInfo = 
			nodeData.getBinding()
				.orElseThrow(() -> new IllegalStateException("SnapshotTreeNode " + node.getNodeKey() + " doesn't have a binding."));
        
		String nodeKey = node.getNodeKey();
    	Optional<String> description = nodeData.getDefinition();
        String bindingStrength = bindingInfo.getStrength();
    	String anchorStrength = bindingStrength.equals("required") ? "code" : bindingStrength;
        Optional<FhirURL> valueSetUrl = bindingInfo.getUrl();
		
        return new StructureDefinitionBindingsTableRowData(nodeKey, description, bindingStrength, anchorStrength, valueSetUrl);
	}
}
