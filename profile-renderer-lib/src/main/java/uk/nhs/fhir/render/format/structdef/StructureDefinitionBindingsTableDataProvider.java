package uk.nhs.fhir.render.format.structdef;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.structdef.BindingInfo;
import uk.nhs.fhir.data.structdef.tree.SnapshotData;
import uk.nhs.fhir.data.structdef.tree.SnapshotTreeNode;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.render.html.table.TableTitle;

public class StructureDefinitionBindingsTableDataProvider {
	private final List<SnapshotTreeNode> nodesWithBindings;
	private final Map<SnapshotTreeNode, List<SnapshotTreeNode>> extensionsWithBindings;
	
	public StructureDefinitionBindingsTableDataProvider(List<SnapshotTreeNode> nodesWithBindings, Map<SnapshotTreeNode, List<SnapshotTreeNode>> extensionsWithBindings) {
		this.nodesWithBindings = nodesWithBindings;
		this.extensionsWithBindings = extensionsWithBindings;
	}
	
	public List<TableTitle> getColumns() {
		return Lists.newArrayList(
			new TableTitle("Path", "Identifies the bound node within the StructureDefinition", "20%"),
			new TableTitle("Definition", "Node description", "40%"),
			new TableTitle("Type", "Type of binding.", "10%"),
			new TableTitle("Reference", "ValueSet containing permitted codes.", "30%"));
	}
	
	public List<StructureDefinitionBindingsTableSection> getSections() {
		List<StructureDefinitionBindingsTableSection> tableSections = Lists.newArrayList();

		for (Entry<SnapshotTreeNode, List<SnapshotTreeNode>> extensionWithBindings : extensionsWithBindings.entrySet()) {
			List<StructureDefinitionBindingsTableRowData> sectionRows = getRows(extensionWithBindings.getValue(), true);
			tableSections.add(new StructureDefinitionBindingsTableSection(extensionWithBindings.getKey(), sectionRows));
		}
		
		tableSections.sort(Comparator.comparing(section -> section.getSourceNode().get().getNodeKey()));

		tableSections.add(0, new StructureDefinitionBindingsTableSection(getRows(nodesWithBindings, false)));
		
		return tableSections;
	}

	private List<StructureDefinitionBindingsTableRowData> getRows(List<SnapshotTreeNode> nodes, boolean isReferencedResource) {
		return nodes
			.stream()
			.map(node -> toRowData(node, isReferencedResource))
			.collect(Collectors.toList());
	}
	
	private StructureDefinitionBindingsTableRowData toRowData(SnapshotTreeNode node, boolean isReferencedResource) {
		SnapshotData nodeData = node.getData();
		BindingInfo bindingInfo = 
			nodeData.getBinding()
				.orElseThrow(() -> new IllegalStateException(node.getNodeKey() + " doesn't have a binding."));
        
		String nodeKey = node.getNodeKey();
    	Optional<String> description = 
    		isReferencedResource ?
    			Optional.of(node.getRoot().getData().getInformation()) :
    			nodeData.getDefinition();
        String bindingStrength = bindingInfo.getStrength();
    	String anchorStrength = bindingStrength.equals("required") ? "code" : bindingStrength;
        Optional<FhirURL> valueSetUrl = bindingInfo.getUrl();
		
        return new StructureDefinitionBindingsTableRowData(nodeKey, description, bindingStrength, anchorStrength, valueSetUrl);
	}
}
