package uk.nhs.fhir.makehtml.render.structdef;

import javax.xml.parsers.ParserConfigurationException;

import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.RendererContext;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;

public class StructureDefinitionFormatter extends ResourceFormatter<WrappedStructureDefinition> {

	public StructureDefinitionFormatter(WrappedStructureDefinition wrappedResource, RendererContext context) {
		super(wrappedResource, context);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		HTMLDocSection metadataPanel = new StructureDefinitionMetadataFormatter(wrappedResource, context).makeSectionHTML();
		HTMLDocSection snapshotTree = new StructureDefinitionSnapshotFormatter(wrappedResource, context).makeSectionHTML();
		HTMLDocSection differentialTree = new StructureDefinitionDifferentialFormatter(wrappedResource, context).makeSectionHTML();
		HTMLDocSection detailsTable = new StructureDefinitionDetailsFormatter(wrappedResource, context).makeSectionHTML();
		HTMLDocSection bindingTable = new StructureDefinitionBindingFormatter(wrappedResource, context).makeSectionHTML();
		
		HTMLDocSection structureDefinitionSection = new HTMLDocSection();
		
		structureDefinitionSection.addSection(metadataPanel);
		structureDefinitionSection.addSection(snapshotTree);
		structureDefinitionSection.addSection(differentialTree);
		structureDefinitionSection.addSection(detailsTable);
		structureDefinitionSection.addSection(bindingTable);
		
		return structureDefinitionSection;
	}

}
