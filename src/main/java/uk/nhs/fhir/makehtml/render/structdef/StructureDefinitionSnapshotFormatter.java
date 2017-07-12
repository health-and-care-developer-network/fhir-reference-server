package uk.nhs.fhir.makehtml.render.structdef;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Element;

import uk.nhs.fhir.makehtml.data.FhirTreeData;
import uk.nhs.fhir.makehtml.data.UnchangedSliceInfoRemover;
import uk.nhs.fhir.makehtml.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.makehtml.html.FhirPanel;
import uk.nhs.fhir.makehtml.html.FhirTreeTable;
import uk.nhs.fhir.makehtml.html.Table;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.TreeTableFormatter;

public class StructureDefinitionSnapshotFormatter extends TreeTableFormatter<WrappedStructureDefinition> {
	
	@Override
	public HTMLDocSection makeSectionHTML(WrappedStructureDefinition structureDefinition) throws ParserConfigurationException {

		HTMLDocSection section = new HTMLDocSection();
		
		StructureDefinitionTreeDataProvider dataProvider = new StructureDefinitionTreeDataProvider(structureDefinition);
		FhirTreeData snapshotTreeData = dataProvider.getSnapshotTreeData();

		boolean isExtension = structureDefinition.isExtension();
		if (!isExtension) {
			FhirTreeData differentialTreeData = isExtension ? null : dataProvider.getDifferentialTreeData(snapshotTreeData);
			new UnchangedSliceInfoRemover(differentialTreeData).process(snapshotTreeData);
			new RedundantValueNodeRemover(differentialTreeData).process(snapshotTreeData);
		}
		
		snapshotTreeData.stripRemovedElements();
		snapshotTreeData.tidyData();
		
		FhirTreeTable snapshotTree = new FhirTreeTable(snapshotTreeData);
		
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
