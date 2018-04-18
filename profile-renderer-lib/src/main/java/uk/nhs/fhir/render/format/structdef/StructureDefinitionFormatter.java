package uk.nhs.fhir.render.format.structdef;

import javax.xml.parsers.ParserConfigurationException;

import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.ResourceFormatter;

public class StructureDefinitionFormatter extends ResourceFormatter<WrappedStructureDefinition> {

	public StructureDefinitionFormatter(WrappedStructureDefinition wrappedResource) {
		super(wrappedResource);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		HTMLDocSection metadataPanel = new StructureDefinitionMetadataFormatter(wrappedResource).makeSectionHTML();
		HTMLDocSection snapshotTree = new StructureDefinitionSnapshotFormatter(wrappedResource).makeSectionHTML();
		HTMLDocSection differentialTree = new StructureDefinitionDifferentialFormatter(wrappedResource).makeSectionHTML();
		HTMLDocSection detailsTable = new StructureDefinitionDetailsFormatter(wrappedResource).makeSectionHTML();
		HTMLDocSection bindingTable = new StructureDefinitionBindingsTableFormatter(wrappedResource).makeSectionHTML();
		
		HTMLDocSection structureDefinitionSection = new HTMLDocSection();
		
		structureDefinitionSection.addSection(metadataPanel);
		structureDefinitionSection.addSection(snapshotTree);
		structureDefinitionSection.addSection(differentialTree);
		structureDefinitionSection.addSection(detailsTable);
		structureDefinitionSection.addSection(bindingTable);
		
		return structureDefinitionSection;
	}

}
