package uk.nhs.fhir.makehtml.render.structdef;

import javax.xml.parsers.ParserConfigurationException;

import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.makehtml.FhirFileRegistry;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;

public class StructureDefinitionFormatter extends ResourceFormatter<WrappedStructureDefinition> {

	public StructureDefinitionFormatter(WrappedStructureDefinition wrappedResource, FhirFileRegistry otherResources) {
		super(wrappedResource, otherResources);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		HTMLDocSection metadataPanel = new StructureDefinitionMetadataFormatter(wrappedResource, otherResources).makeSectionHTML();
		HTMLDocSection snapshotTree = new StructureDefinitionSnapshotFormatter(wrappedResource, otherResources).makeSectionHTML();
		HTMLDocSection differentialTree = new StructureDefinitionDifferentialFormatter(wrappedResource, otherResources).makeSectionHTML();
		HTMLDocSection detailsTable = new StructureDefinitionDetailsFormatter(wrappedResource, otherResources).makeSectionHTML();
		HTMLDocSection bindingTable = new StructureDefinitionBindingFormatter(wrappedResource, otherResources).makeSectionHTML();
		
		HTMLDocSection structureDefinitionSection = new HTMLDocSection();
		
		structureDefinitionSection.addSection(metadataPanel);
		structureDefinitionSection.addSection(snapshotTree);
		structureDefinitionSection.addSection(differentialTree);
		structureDefinitionSection.addSection(detailsTable);
		structureDefinitionSection.addSection(bindingTable);
		
		return structureDefinitionSection;
	}

}
