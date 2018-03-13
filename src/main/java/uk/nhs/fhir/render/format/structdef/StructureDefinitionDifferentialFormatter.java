package uk.nhs.fhir.render.format.structdef;

import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Element;

import uk.nhs.fhir.data.structdef.tree.DifferentialData;
import uk.nhs.fhir.data.structdef.tree.DifferentialTreeNode;
import uk.nhs.fhir.data.structdef.tree.FhirTreeData;
import uk.nhs.fhir.data.structdef.tree.tidy.ChildlessDummyNodeRemover;
import uk.nhs.fhir.data.structdef.tree.tidy.ComplexExtensionChildrenStripper;
import uk.nhs.fhir.data.structdef.tree.tidy.ExtensionsSlicingNodesRemover;
import uk.nhs.fhir.data.structdef.tree.tidy.UnwantedConstraintRemover;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.render.RendererContext;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.TreeTableFormatter;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.table.Table;
import uk.nhs.fhir.render.html.tree.FhirTreeTable;
import uk.nhs.fhir.util.StructureDefinitionRepository;

public class StructureDefinitionDifferentialFormatter extends TreeTableFormatter<WrappedStructureDefinition> {

	public StructureDefinitionDifferentialFormatter(WrappedStructureDefinition wrappedResource) {
		super(wrappedResource);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		HTMLDocSection section = new HTMLDocSection();
		
		StructureDefinitionRepository structureDefinitions = RendererContext.forThread().getFhirFileRegistry();
		FhirTreeData<DifferentialData, DifferentialTreeNode> differentialTreeData = wrappedResource.getDifferentialTree(Optional.of(structureDefinitions));
		
		new ExtensionsSlicingNodesRemover<>(differentialTreeData).process();
		new ChildlessDummyNodeRemover<>(differentialTreeData).process();
		new UnwantedConstraintRemover<>(differentialTreeData).process();
		new ComplexExtensionChildrenStripper<>(differentialTreeData).process();
		
		FhirTreeTable<DifferentialData, DifferentialTreeNode> differentialTreeTable = new FhirTreeTable<>(differentialTreeData, getResourceVersion());

		Table differentialTable = differentialTreeTable.asTable();
		
		Element differentialHtmlTable = differentialTable.makeTable();
		
		getTableBackgroundStyles(differentialHtmlTable).forEach(section::addStyle);
		
		addStyles(section);
		differentialTreeTable.getStyles().forEach(section::addStyle);
		section.addBodyElement(new FhirPanel(differentialHtmlTable).makePanel());
		
		return section;
	}
}
