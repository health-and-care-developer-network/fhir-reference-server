package uk.nhs.fhir.makehtml.render.structdef;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Element;

import uk.nhs.fhir.data.structdef.tree.FhirTreeData;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.makehtml.FhirFileRegistry;
import uk.nhs.fhir.makehtml.html.panel.FhirPanel;
import uk.nhs.fhir.makehtml.html.table.Table;
import uk.nhs.fhir.makehtml.html.tree.FhirTreeTable;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.TreeTableFormatter;

public class StructureDefinitionSnapshotFormatter extends TreeTableFormatter<WrappedStructureDefinition> {
	
	public StructureDefinitionSnapshotFormatter(WrappedStructureDefinition wrappedResource, FhirFileRegistry otherResources) {
		super(wrappedResource, otherResources);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {

		HTMLDocSection section = new HTMLDocSection();
		
		StructureDefinitionTreeDataProvider dataProvider = new StructureDefinitionTreeDataProvider(wrappedResource, otherResources);
		FhirTreeData snapshotTreeData = dataProvider.getSnapshotTreeData();

		boolean isExtension = wrappedResource.isExtension();
		if (!isExtension) {
			FhirTreeData differentialTreeData = isExtension ? null : dataProvider.getDifferentialTreeData(snapshotTreeData);
			new UnchangedSliceInfoRemover(differentialTreeData).process(snapshotTreeData);
			new RedundantValueNodeRemover(differentialTreeData).process(snapshotTreeData);
		}
		
		snapshotTreeData.stripRemovedElements();
		snapshotTreeData.tidyData();
		
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
