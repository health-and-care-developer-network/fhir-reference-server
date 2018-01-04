package uk.nhs.fhir.render.format.structdef;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Element;

import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.TreeTableFormatter;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.table.Table;
import uk.nhs.fhir.render.html.tree.FhirTreeTable;
import uk.nhs.fhir.render.tree.AbstractFhirTreeTableContent;
import uk.nhs.fhir.render.tree.FhirTreeData;
import uk.nhs.fhir.render.tree.tidy.ChildlessDummyNodeRemover;
import uk.nhs.fhir.render.tree.tidy.ComplexExtensionChildrenStripper;
import uk.nhs.fhir.render.tree.tidy.ExtensionsSlicingNodesRemover;
import uk.nhs.fhir.render.tree.tidy.UnwantedConstraintRemover;

public class StructureDefinitionDifferentialFormatter extends TreeTableFormatter<WrappedStructureDefinition> {

	public StructureDefinitionDifferentialFormatter(WrappedStructureDefinition wrappedResource) {
		super(wrappedResource);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		HTMLDocSection section = new HTMLDocSection();

		StructureDefinitionTreeDataProvider dataProvider = new StructureDefinitionTreeDataProvider(wrappedResource);
		
		FhirTreeData<AbstractFhirTreeTableContent> differentialTreeData = dataProvider.getDifferentialTreeData();
		
		new ExtensionsSlicingNodesRemover(differentialTreeData).process();
		new ChildlessDummyNodeRemover(differentialTreeData).process();
		new UnwantedConstraintRemover(differentialTreeData).process();
		new ComplexExtensionChildrenStripper(differentialTreeData).process();
		
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
