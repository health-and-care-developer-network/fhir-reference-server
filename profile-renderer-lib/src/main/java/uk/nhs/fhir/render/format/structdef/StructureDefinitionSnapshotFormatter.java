package uk.nhs.fhir.render.format.structdef;

import java.util.Optional;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Element;

import uk.nhs.fhir.data.structdef.tree.DifferentialData;
import uk.nhs.fhir.data.structdef.tree.DifferentialTreeNode;
import uk.nhs.fhir.data.structdef.tree.FhirTreeData;
import uk.nhs.fhir.data.structdef.tree.SnapshotData;
import uk.nhs.fhir.data.structdef.tree.SnapshotTreeNode;
import uk.nhs.fhir.data.structdef.tree.tidy.ChildlessDummyNodeRemover;
import uk.nhs.fhir.data.structdef.tree.tidy.ComplexExtensionChildrenStripper;
import uk.nhs.fhir.data.structdef.tree.tidy.DefaultElementStripper;
import uk.nhs.fhir.data.structdef.tree.tidy.ExtensionsSlicingNodesRemover;
import uk.nhs.fhir.data.structdef.tree.tidy.RedundantValueNodeRemover;
import uk.nhs.fhir.data.structdef.tree.tidy.RemovedElementStripper;
import uk.nhs.fhir.data.structdef.tree.tidy.UnchangedSliceInfoRemover;
import uk.nhs.fhir.data.structdef.tree.tidy.UnwantedConstraintRemover;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.render.RendererContext;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.TreeTableFormatter;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.table.Table;
import uk.nhs.fhir.render.html.tree.FhirTreeTable;

public class StructureDefinitionSnapshotFormatter extends TreeTableFormatter<WrappedStructureDefinition> {
	
	public StructureDefinitionSnapshotFormatter(WrappedStructureDefinition wrappedResource) {
		super(wrappedResource);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {

		HTMLDocSection section = new HTMLDocSection();

		Set<String> permittedMissingExtensionPrefixes = RendererContext.forThread().getPermittedMissingExtensionPrefixes();
		FhirTreeData<SnapshotData, SnapshotTreeNode> snapshotTreeData = wrappedResource.getSnapshotTree(Optional.of(RendererContext.forThread().getFhirFileRegistry()), permittedMissingExtensionPrefixes);

		boolean isExtension = wrappedResource.isExtension();
		if (!isExtension) {
			FhirTreeData<DifferentialData, DifferentialTreeNode> differentialTreeData = wrappedResource.getDifferentialTree(Optional.of(RendererContext.forThread().getFhirFileRegistry()), permittedMissingExtensionPrefixes);
			new DefaultElementStripper<>(differentialTreeData).process(snapshotTreeData);
			new UnchangedSliceInfoRemover<>(differentialTreeData).process(snapshotTreeData);
			new RedundantValueNodeRemover<>(differentialTreeData).process(snapshotTreeData);
			new ChildlessDummyNodeRemover<>(differentialTreeData).process();
		}

		new RemovedElementStripper<>(snapshotTreeData).process();
		new ExtensionsSlicingNodesRemover<>(snapshotTreeData).process();
		new UnwantedConstraintRemover<>(snapshotTreeData).process();
		new ComplexExtensionChildrenStripper<>(snapshotTreeData).process();
		
		FhirTreeTable<SnapshotData, SnapshotTreeNode> snapshotTree = new FhirTreeTable<>(snapshotTreeData, getResourceVersion());
		
		Table snapshotTable = snapshotTree.asTable();
//		Element snapshotHtmlTable = snapshotTable.makeTable();
		Element snapshotHtmlTable = snapshotTable.makeTable_collapse();

		addStyles(section);
		getTableBackgroundStyles(snapshotHtmlTable).forEach(section::addStyle);
		snapshotTree.getStyles().forEach(section::addStyle);
		
		section.addBodyElement(new FhirPanel(snapshotHtmlTable).makePanel());
		
		return section;
	}

	protected void addStyles(HTMLDocSection section) {
		super.addStyles(section);
		StructureDefinitionMetadataFormatter.getStyles().forEach(section::addStyle);
	}
}
