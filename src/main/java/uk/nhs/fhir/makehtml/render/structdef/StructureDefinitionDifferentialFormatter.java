package uk.nhs.fhir.makehtml.render.structdef;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Element;

import uk.nhs.fhir.makehtml.data.FhirTreeData;
import uk.nhs.fhir.makehtml.data.ResourceSectionType;
import uk.nhs.fhir.makehtml.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.makehtml.html.FhirPanel;
import uk.nhs.fhir.makehtml.html.FhirTreeTable;
import uk.nhs.fhir.makehtml.html.Table;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.TreeTableFormatter;

public class StructureDefinitionDifferentialFormatter extends TreeTableFormatter<WrappedStructureDefinition> {

	public StructureDefinitionDifferentialFormatter(WrappedStructureDefinition wrappedResource) {
		super(wrappedResource);
		this.resourceSectionType = ResourceSectionType.DIFFERENTIAL;
	}

	@Override
	public HTMLDocSection makeSectionHTML(WrappedStructureDefinition structureDefinition) throws ParserConfigurationException {
		HTMLDocSection section = new HTMLDocSection();

		StructureDefinitionTreeDataProvider dataProvider = new StructureDefinitionTreeDataProvider(structureDefinition);
		
		FhirTreeData differentialTreeData = dataProvider.getDifferentialTreeData();
		differentialTreeData.tidyData();
		FhirTreeTable differentialTreeTable = new FhirTreeTable(differentialTreeData, getResourceVersion());
		
		Table differentialTable = differentialTreeTable.asTable();
		
		Element differentialHtmlTable = differentialTable.makeTable();
		
		getTableBackgroundStyles(differentialHtmlTable).forEach(section::addStyle);
		
		addStyles(section);
		differentialTreeTable.getStyles().forEach(section::addStyle);
		section.addBodyElement(new FhirPanel(differentialHtmlTable).makePanel());
		
		return section;
	}
}
