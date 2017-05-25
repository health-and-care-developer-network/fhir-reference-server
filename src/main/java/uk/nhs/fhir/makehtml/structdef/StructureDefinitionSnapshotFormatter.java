package uk.nhs.fhir.makehtml.structdef;

import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jdom2.Element;

import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import uk.nhs.fhir.makehtml.HTMLDocSection;
import uk.nhs.fhir.makehtml.TreeTableFormatter;
import uk.nhs.fhir.makehtml.data.FhirTreeData;
import uk.nhs.fhir.makehtml.html.FhirPanel;
import uk.nhs.fhir.makehtml.html.FhirTreeTable;
import uk.nhs.fhir.makehtml.html.Table;

public class StructureDefinitionSnapshotFormatter extends TreeTableFormatter {
	
	@Override
	public HTMLDocSection makeSectionHTML(IBaseResource source) throws ParserConfigurationException {
		StructureDefinition structureDefinition = (StructureDefinition)source;

		boolean isExtension = structureDefinition.getConstrainedType().equals("Extension");
		
		HTMLDocSection section = new HTMLDocSection();
		
		StructureDefinitionTreeDataProvider dataProvider = new StructureDefinitionTreeDataProvider(structureDefinition);
		FhirTreeData snapshotTreeData = dataProvider.getSnapshotTreeData();
		
		/**
		 * The differential data is used to remove children of slicing nodes which are not modified
		 * from the base resource.
		 */
		FhirTreeData differentialTreeData = isExtension ? null : dataProvider.getDifferentialTreeData(snapshotTreeData);
		
		FhirTreeTable snapshotTree = new FhirTreeTable(snapshotTreeData);
		Table snapshotTable = snapshotTree.asTable(false, Optional.ofNullable(differentialTreeData));
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
