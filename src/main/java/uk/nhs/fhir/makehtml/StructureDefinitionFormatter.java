package uk.nhs.fhir.makehtml;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Element;

import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import uk.nhs.fhir.makehtml.html.FhirPanel;
import uk.nhs.fhir.makehtml.html.FhirTreeTable;
import uk.nhs.fhir.makehtml.html.LinkCell;
import uk.nhs.fhir.makehtml.html.ResourceFlagsCell;
import uk.nhs.fhir.makehtml.html.SharedCSS;
import uk.nhs.fhir.makehtml.html.Table;
import uk.nhs.fhir.makehtml.html.ValueWithInfoCell;

public class StructureDefinitionFormatter extends ResourceFormatter<StructureDefinition> {

	@Override
	public HTMLDocSection makeSectionHTML(StructureDefinition source) throws ParserConfigurationException {
		
		StructureDefinitionTreeDataProvider dataProvider = new StructureDefinitionTreeDataProvider(source, fhirDocLinkFactory);
		FhirTreeTable formattedTree = new FhirTreeTable(dataProvider.getTreeData());

		Table fhirTable = formattedTree.asTable(false);
		Element formattedTable = fhirTable.makeTable();
		Element panelElement = new FhirPanel(formattedTable).makePanel();
		
		HTMLDocSection section = new HTMLDocSection();
		addStyles(section);
		formattedTree.getStyles().forEach(section::addStyle);
		section.addBodyElement(panelElement);
		return section;
	}

	private void addStyles(HTMLDocSection section) {
		SharedCSS.getTableStyles().forEach(section::addStyle);
		SharedCSS.getPanelStyles().forEach(section::addStyle);
		ValueWithInfoCell.getStyles().forEach(section::addStyle);
		LinkCell.getStyles().forEach(section::addStyle);
		ResourceFlagsCell.getStyles().forEach(section::addStyle);
	}
}
