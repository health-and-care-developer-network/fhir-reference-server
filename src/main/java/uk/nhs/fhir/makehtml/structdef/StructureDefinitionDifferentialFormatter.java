package uk.nhs.fhir.makehtml.structdef;

import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Element;

import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import uk.nhs.fhir.makehtml.HTMLDocSection;
import uk.nhs.fhir.makehtml.TreeTableFormatter;
import uk.nhs.fhir.makehtml.html.FhirPanel;
import uk.nhs.fhir.makehtml.html.FhirTreeTable;
import uk.nhs.fhir.makehtml.html.Table;

public class StructureDefinitionDifferentialFormatter extends TreeTableFormatter<StructureDefinition> {

	@Override
	public HTMLDocSection makeSectionHTML(StructureDefinition source) throws ParserConfigurationException {
		HTMLDocSection section = new HTMLDocSection();

		StructureDefinitionTreeDataProvider dataProvider = new StructureDefinitionTreeDataProvider(source);
		
		FhirTreeTable differentialTreeData = new FhirTreeTable(dataProvider.getDifferentialTreeData());
		Table differentialTable = differentialTreeData.asTable(true, Optional.empty());
		Element differentialHtmlTable = differentialTable.makeTable();
		
		getTableBackgroundStyles(differentialHtmlTable).forEach(section::addStyle);
		
		section.addBodyElement(new FhirPanel(differentialHtmlTable).makePanel());
		
		return section;
	}

}
