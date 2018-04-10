package uk.nhs.fhir.render.format.structdef;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jdom2.Element;

import uk.nhs.fhir.data.structdef.tree.SnapshotTreeNode;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.render.RendererContext;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.TableFormatter;
import uk.nhs.fhir.render.html.cell.LinkCell;
import uk.nhs.fhir.render.html.cell.ResourceFlagsCell;
import uk.nhs.fhir.render.html.cell.ValueWithInfoCell;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.table.Table;
import uk.nhs.fhir.render.html.table.TableRow;
import uk.nhs.fhir.util.FhirFileRegistry;

public class StructureDefinitionBindingsTableFormatter extends TableFormatter<WrappedStructureDefinition> {
	
	public StructureDefinitionBindingsTableFormatter(WrappedStructureDefinition structureDefinition) {
		super(structureDefinition);
	}

	@Override
	public HTMLDocSection makeSectionHTML() {
		List<SnapshotTreeNode> nodesWithBindings = getNodesWithBindings(wrappedResource);
		Map<SnapshotTreeNode, List<SnapshotTreeNode>> extensionsWithBindings = getExtensionsWithBindings(wrappedResource);
		
		if (nodesWithBindings.isEmpty()
		  && extensionsWithBindings.isEmpty()) {
			return null;
		} else {
			HTMLDocSection section = new HTMLDocSection();
			Element bindingsPanel = buildBindingsPanel(nodesWithBindings, extensionsWithBindings);
			section.addBodyElement(bindingsPanel);

			addStyles(section);
			return section;
		}
	}
	
	private Map<SnapshotTreeNode, List<SnapshotTreeNode>> getExtensionsWithBindings(WrappedStructureDefinition wrappedResource) {
		FhirFileRegistry fileRegistry = RendererContext.forThread().getFhirFileRegistry();
		return wrappedResource.getExtensionsWithBindings(fileRegistry);
	}

	private List<SnapshotTreeNode> getNodesWithBindings(WrappedStructureDefinition wrappedResource) {
		return StreamSupport.stream(wrappedResource
        	.getSnapshotTree(Optional.of(RendererContext.forThread().getFhirFileRegistry()))
        	.nodes().spliterator(), false)
        	.filter(node -> 
        		!node.isRemovedByProfile()
              	  && node.getData().getBinding().isPresent())
        	.collect(Collectors.toList());
	}

	private Element buildBindingsPanel(List<SnapshotTreeNode> nodesWithBindings, Map<SnapshotTreeNode, List<SnapshotTreeNode>> extensionsWithBindings) {
		StructureDefinitionBindingsTableDataProvider tableData = new StructureDefinitionBindingsTableDataProvider(nodesWithBindings, extensionsWithBindings);
		
		List<StructureDefinitionBindingsTableSection> sections = tableData.getSections();
		
		List<TableRow> tableRows = 
			new StructureDefinitionBindingsTableSectionFormatter(getResourceVersion())
				.formatSections(sections);
		
		Element bindingsTable = new Table(tableData.getColumns(), tableRows).makeTable();
		return new FhirPanel("Bindings", bindingsTable).makePanel();
	}

    public void addStyles(HTMLDocSection section) {
        Table.getStyles().forEach(section::addStyle);
        FhirPanel.getStyles().forEach(section::addStyle);
        ValueWithInfoCell.getStyles().forEach(section::addStyle);
        LinkCell.getStyles().forEach(section::addStyle);
        ResourceFlagsCell.getStyles().forEach(section::addStyle);
        StructureDefinitionMetadataFormatter.getStyles().forEach(section::addStyle);
    }
}
