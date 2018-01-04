package uk.nhs.fhir.render.format.structdef;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Element;

import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.TreeTableFormatter;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.table.Table;
import uk.nhs.fhir.render.html.tree.FhirTreeTable;
import uk.nhs.fhir.render.tree.FhirTreeData;
import uk.nhs.fhir.render.tree.tidy.ChildlessDummyNodeRemover;
import uk.nhs.fhir.render.tree.tidy.ComplexExtensionChildrenStripper;
import uk.nhs.fhir.render.tree.tidy.ExtensionsSlicingNodesRemover;
import uk.nhs.fhir.render.tree.tidy.RemovedElementStripper;
import uk.nhs.fhir.render.tree.tidy.UnwantedConstraintRemover;

public class StructureDefinitionSnapshotFormatter extends TreeTableFormatter<WrappedStructureDefinition> {
	
	public StructureDefinitionSnapshotFormatter(WrappedStructureDefinition wrappedResource) {
		super(wrappedResource);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {

		HTMLDocSection section = new HTMLDocSection();
		
		StructureDefinitionTreeDataProvider dataProvider = new StructureDefinitionTreeDataProvider(wrappedResource);
		FhirTreeData snapshotTreeData = dataProvider.getSnapshotTreeData();

		boolean isExtension = wrappedResource.isExtension();
		if (!isExtension) {
			FhirTreeData differentialTreeData = isExtension ? null : dataProvider.getDifferentialTreeData(snapshotTreeData);
			new UnchangedSliceInfoRemover(differentialTreeData).process(snapshotTreeData);
			new RedundantValueNodeRemover(differentialTreeData).process(snapshotTreeData);
		}

		new RemovedElementStripper(snapshotTreeData).process();
		new ExtensionsSlicingNodesRemover(snapshotTreeData).process();
		new ChildlessDummyNodeRemover(snapshotTreeData).process();
		new UnwantedConstraintRemover(snapshotTreeData).process();
		new ComplexExtensionChildrenStripper(snapshotTreeData).process();
		
		FhirTreeTable snapshotTree = new FhirTreeTable(snapshotTreeData, getResourceVersion());
		
		Table snapshotTable = snapshotTree.asTable();
		Element snapshotHtmlTable = snapshotTable.makeTable();

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
