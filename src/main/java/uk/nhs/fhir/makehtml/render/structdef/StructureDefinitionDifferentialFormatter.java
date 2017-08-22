package uk.nhs.fhir.makehtml.render.structdef;

import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jdom2.Element;

import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import uk.nhs.fhir.makehtml.data.FhirTreeData;
import uk.nhs.fhir.makehtml.data.ResourceSectionType;
import uk.nhs.fhir.makehtml.html.FhirPanel;
import uk.nhs.fhir.makehtml.html.FhirTreeTable;
import uk.nhs.fhir.makehtml.html.Table;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.TreeTableFormatter;

public class StructureDefinitionDifferentialFormatter extends TreeTableFormatter {

	public StructureDefinitionDifferentialFormatter() { this.resourceSectionType = ResourceSectionType.DIFFERENTIAL; }

	@Override
	public HTMLDocSection makeSectionHTML(IBaseResource source) throws ParserConfigurationException {
		StructureDefinition structureDefinition = (StructureDefinition)source;
		HTMLDocSection section = new HTMLDocSection();

		StructureDefinitionTreeDataProvider dataProvider = new StructureDefinitionTreeDataProvider(structureDefinition);
		
		FhirTreeData differentialTreeData = dataProvider.getDifferentialTreeData();
		differentialTreeData.tidyData();
		FhirTreeTable differentialTreeTable = new FhirTreeTable(differentialTreeData);
		
		Table differentialTable = differentialTreeTable.asTable();
		
		Element differentialHtmlTable = differentialTable.makeTable();
		
		getTableBackgroundStyles(differentialHtmlTable).forEach(section::addStyle);
		
		addStyles(section);
		differentialTreeTable.getStyles().forEach(section::addStyle);
		section.addBodyElement(new FhirPanel(differentialHtmlTable).makePanel());
		
		return section;
	}
}
