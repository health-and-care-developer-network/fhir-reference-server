package uk.nhs.fhir.makehtml.render.structdef;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Element;

import uk.nhs.fhir.data.structdef.tree.FhirTreeData;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.makehtml.html.panel.FhirPanel;
import uk.nhs.fhir.makehtml.html.table.Table;
import uk.nhs.fhir.makehtml.html.tree.FhirTreeTable;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.RendererContext;
import uk.nhs.fhir.makehtml.render.TreeTableFormatter;

public class StructureDefinitionDifferentialFormatter extends TreeTableFormatter<WrappedStructureDefinition> {

	public StructureDefinitionDifferentialFormatter(WrappedStructureDefinition wrappedResource, RendererContext context) {
		super(wrappedResource, context);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		HTMLDocSection section = new HTMLDocSection();

		StructureDefinitionTreeDataProvider dataProvider = new StructureDefinitionTreeDataProvider(wrappedResource, context);
		
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
