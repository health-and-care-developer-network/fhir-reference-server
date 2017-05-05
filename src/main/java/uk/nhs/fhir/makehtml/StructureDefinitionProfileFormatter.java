package uk.nhs.fhir.makehtml;

import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Element;

import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import uk.nhs.fhir.makehtml.data.FhirTreeData;
import uk.nhs.fhir.makehtml.html.FhirPanel;
import uk.nhs.fhir.makehtml.html.FhirTreeTable;
import uk.nhs.fhir.makehtml.html.StructureDefinitionMetadataFormatter;
import uk.nhs.fhir.makehtml.html.Table;

public class StructureDefinitionProfileFormatter extends TreeTableFormatter<StructureDefinition> {
	
	@Override
	public HTMLDocSection makeSectionHTML(StructureDefinition source) throws ParserConfigurationException {

		boolean isExtension = source.getConstrainedType().equals("Extension");
		
		HTMLDocSection section = new HTMLDocSection();
		
		StructureDefinitionTreeDataProvider dataProvider = new StructureDefinitionTreeDataProvider(source);
		FhirTreeTable snapshotTree = new FhirTreeTable(dataProvider.getSnapshotTreeData());
		
		/*
		 * The differential data is used to remove children of slicing nodes which are not modified
		 * from the base resource.
		 */
		FhirTreeData differentialTreeData2 = dataProvider.getDifferentialTreeData();
		FhirTreeData differentialTreeData = isExtension ? null : differentialTreeData2;
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
