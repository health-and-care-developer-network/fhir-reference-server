package uk.nhs.fhir.makehtml.render.structdef;

import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jdom2.Element;

import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import uk.nhs.fhir.makehtml.data.FhirTreeData;
import uk.nhs.fhir.makehtml.data.UnchangedSliceInfoRemover;
import uk.nhs.fhir.makehtml.html.FhirPanel;
import uk.nhs.fhir.makehtml.html.FhirTreeTable;
import uk.nhs.fhir.makehtml.html.Table;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.TreeTableFormatter;

public class StructureDefinitionSnapshotFormatter extends TreeTableFormatter {
	
	@Override
	public HTMLDocSection makeSectionHTML(IBaseResource source) throws ParserConfigurationException {
		StructureDefinition structureDefinition = (StructureDefinition)source;

		HTMLDocSection section = new HTMLDocSection();
		
		StructureDefinitionTreeDataProvider dataProvider = new StructureDefinitionTreeDataProvider(structureDefinition);
		FhirTreeData snapshotTreeData = dataProvider.getSnapshotTreeData();

		boolean isExtension = structureDefinition.getConstrainedType().equals("Extension");
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
