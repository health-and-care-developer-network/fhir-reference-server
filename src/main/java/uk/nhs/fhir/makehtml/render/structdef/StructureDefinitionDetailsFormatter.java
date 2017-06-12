package uk.nhs.fhir.makehtml.render.structdef;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import uk.nhs.fhir.html.jdom2.Elements;
import uk.nhs.fhir.makehtml.data.BindingInfo;
import uk.nhs.fhir.makehtml.data.ConstraintInfo;
import uk.nhs.fhir.makehtml.data.FhirTreeData;
import uk.nhs.fhir.makehtml.data.FhirTreeNode;
import uk.nhs.fhir.makehtml.data.FhirTreeTableContent;
import uk.nhs.fhir.makehtml.data.LinkData;
import uk.nhs.fhir.makehtml.data.ResourceFlags;
import uk.nhs.fhir.makehtml.html.FhirCSS;
import uk.nhs.fhir.makehtml.html.FhirPanel;
import uk.nhs.fhir.makehtml.html.LinkCell;
import uk.nhs.fhir.makehtml.html.Table;
import uk.nhs.fhir.makehtml.html.style.CSSRule;
import uk.nhs.fhir.makehtml.html.style.CSSStyleBlock;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;

public class StructureDefinitionDetailsFormatter extends ResourceFormatter {

	@Override
	public HTMLDocSection makeSectionHTML(IBaseResource source) throws ParserConfigurationException {
		StructureDefinition structureDefinition = (StructureDefinition)source;
		HTMLDocSection section = new HTMLDocSection();
		
		Element metadataPanel = getDetailsPanel(structureDefinition);
		section.addBodyElement(metadataPanel);

		getStyles().forEach(section::addStyle);
		Table.getStyles().forEach(section::addStyle);
		FhirPanel.getStyles().forEach(section::addStyle);
		LinkCell.getStyles().forEach(section::addStyle);
		StructureDefinitionDetails.getStyles().forEach(section::addStyle);
		
		return section;
	}

	private Element getDetailsPanel(StructureDefinition structureDefinition) {
		StructureDefinitionTreeDataProvider dataProvider = new StructureDefinitionTreeDataProvider(structureDefinition);
		FhirTreeData snapshotTreeData = dataProvider.getSnapshotTreeData();
		FhirTreeData differentialTreeData = dataProvider.getDifferentialTreeData(snapshotTreeData);
		
		snapshotTreeData.stripRemovedElements();
		
		LinkedHashMap<String, StructureDefinitionDetails> details = Maps.newLinkedHashMap();
		
		for (FhirTreeTableContent node : snapshotTreeData) {
			FhirTreeNode fhirTreeNode = (FhirTreeNode)node;
			
			String pathName = fhirTreeNode.getPathName();
			String key = fhirTreeNode.getNodeKey();
			Optional<String> definition = fhirTreeNode.getDefinition();
			String cardinality = fhirTreeNode.getCardinality().toString();
			Optional<BindingInfo> binding = fhirTreeNode.getBinding();
			List<LinkData> typeLinks = fhirTreeNode.getTypeLinks();
			Optional<String> requirements = fhirTreeNode.getRequirements();
			List<String> aliases = fhirTreeNode.getAliases();
			ResourceFlags resourceFlags = fhirTreeNode.getResourceFlags();
			Optional<String> comments = fhirTreeNode.getComments();
			
			List<ConstraintInfo> inheritedConstraints = Lists.newArrayList();
			List<ConstraintInfo> profileConstraints = Lists.newArrayList();
			splitConstraints((FhirTreeNode)node, differentialTreeData, inheritedConstraints, profileConstraints);
			
			StructureDefinitionDetails detail = new StructureDefinitionDetails(pathName, key, definition, cardinality, binding, typeLinks,
				requirements, aliases, resourceFlags, comments, node.getSlicingInfo(), inheritedConstraints, profileConstraints);

			if (!details.containsKey(key)) {
				details.put(key, detail);
			} else {
				StructureDefinitionDetails existingDetails = details.get(key);
				existingDetails.assertEqualTo(detail);
			}
		}
		
		List<Element> tableContent = Lists.newArrayList();
		for (StructureDefinitionDetails detail : details.values()) {
			detail.addContent(tableContent);
		}
		
		Element table = 
			Elements.withAttributeAndChildren("table",
				new Attribute("class", FhirCSS.TABLE),
				tableContent);
		
		FhirPanel panel = new FhirPanel("Details", table);
		
		return panel.makePanel();
	}

	private void splitConstraints(FhirTreeNode node, FhirTreeData differentialTreeData,
			List<ConstraintInfo> inheritedConstraints, List<ConstraintInfo> profileConstraints) {
		
		Optional<FhirTreeTableContent> matchingDifferentialNode = StreamSupport.stream(differentialTreeData.spliterator(), false)
			.filter(differentialNode -> differentialNode.getBackupNode().get().equals(node))
			.findFirst();
		
		if (matchingDifferentialNode.isPresent()) {
			List<ConstraintInfo> differentialConstraints = matchingDifferentialNode.get().getConstraints();
			
			for (ConstraintInfo constraint : node.getConstraints()) {
				if (differentialConstraints.stream()
						.anyMatch(diffConstraint -> diffConstraint.getKey().equals(constraint.getKey()))) {
					profileConstraints.add(constraint);
				} else {
					inheritedConstraints.add(constraint);
				}
			}
		} else {
			// If not in the differential, it hasn't changed. Any constraints must be inherited.
			inheritedConstraints.addAll(node.getConstraints());
		}
	}
	
	public static List<CSSStyleBlock> getStyles() {
		List<CSSStyleBlock> styles = Lists.newArrayList();
		
		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList("." + FhirCSS.DETAILS_HEADER_ROW),
				Lists.newArrayList(
					new CSSRule("font-size", "12px"),
					new CSSRule("font-family", "verdana"),
					new CSSRule("padding", "3px"),
					new CSSRule("vertical-align", "top"),
					
					new CSSRule("line-height", "1.4em"),
					new CSSRule("font-weight", "normal"),
					new CSSRule("text-align", "left"),
					new CSSRule("border-bottom", "1px silver solid"),
					new CSSRule("background", "#EFEFEF"))));
		
		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList("." + FhirCSS.DETAILS_DATA_ROW, "." + FhirCSS.DETAILS_DATA_CELL),
				Lists.newArrayList(
					new CSSRule("padding", "3px"),
					new CSSRule("font-family", "verdana"),
					new CSSRule("font-size", "11px"),
					new CSSRule("line-height", "1.2em"),
					new CSSRule("font-weight", "normal"),
					new CSSRule("vertical-align", "top"),
					new CSSRule("text-align", "left"))));
		
		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList("." + FhirCSS.DETAILS_HEADER_CELL),
				Lists.newArrayList(
					new CSSRule("font-size", "12px"),
					new CSSRule("font-family", "verdana"),
					new CSSRule("padding", "3px"),
					new CSSRule("vertical-align", "top"),
					
					new CSSRule("line-height", "1.2em"),
					new CSSRule("font-weight", "bold"),
					new CSSRule("border-bottom", "1px silver solid"),
					new CSSRule("background", "#EFEFEF"))));
		
		return styles;
	}
}
