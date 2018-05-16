package uk.nhs.fhir.render.format.githistory;

import java.util.List;

import com.google.common.collect.Lists;

import uk.nhs.fhir.render.html.table.TableTitle;

public class GitHistoryTableDataProvider {
	
	private final String repoName;
	private final String fileName;
	
	public GitHistoryTableDataProvider(String repoName, String fileName) {
		this.repoName = repoName;
		this.fileName = fileName;
	}
	
	public List<TableTitle> getColumns() {
		return Lists.newArrayList(
			new TableTitle("Date", "Date of Commit", "15%"),
			new TableTitle("Author", "Author of change", "15%"),
			new TableTitle("Committer", "User that committed the change", "15%"),
			new TableTitle("Commit Comment", "Comment from Git Commit", "40%"),
			new TableTitle("Commit Details Link", "Link to full details of commit", "15%"));
	}
	
/*	public List<StructureDefinitionBindingsTableSection> getSections() {
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
	}*/
}
