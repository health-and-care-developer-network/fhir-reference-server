package uk.nhs.fhir.makehtml.render.structdef;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import uk.nhs.fhir.data.structdef.BindingInfo;
import uk.nhs.fhir.data.structdef.ConstraintInfo;
import uk.nhs.fhir.data.structdef.ResourceFlags;
import uk.nhs.fhir.data.structdef.tree.FhirTreeData;
import uk.nhs.fhir.data.structdef.tree.FhirTreeNode;
import uk.nhs.fhir.data.structdef.tree.FhirTreeTableContent;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.makehtml.FhirFileRegistry;
import uk.nhs.fhir.makehtml.html.cell.LinkCell;
import uk.nhs.fhir.makehtml.html.jdom2.Elements;
import uk.nhs.fhir.makehtml.html.panel.FhirPanel;
import uk.nhs.fhir.makehtml.html.style.CSSRule;
import uk.nhs.fhir.makehtml.html.style.CSSStyleBlock;
import uk.nhs.fhir.makehtml.html.style.CSSTag;
import uk.nhs.fhir.makehtml.html.style.FhirCSS;
import uk.nhs.fhir.makehtml.html.style.FhirColour;
import uk.nhs.fhir.makehtml.html.style.FhirFont;
import uk.nhs.fhir.makehtml.html.table.Table;
import uk.nhs.fhir.makehtml.render.HTMLDocSection;
import uk.nhs.fhir.makehtml.render.ResourceFormatter;

public class StructureDefinitionDetailsFormatter extends ResourceFormatter<WrappedStructureDefinition> {

	public StructureDefinitionDetailsFormatter(WrappedStructureDefinition wrappedResource, FhirFileRegistry otherResources) {
		super(wrappedResource, otherResources);
	}

	@Override
	public HTMLDocSection makeSectionHTML() throws ParserConfigurationException {
		HTMLDocSection section = new HTMLDocSection();
		
		Element metadataPanel = getDetailsPanel();
		section.addBodyElement(metadataPanel);

		getStyles().forEach(section::addStyle);
		Table.getStyles().forEach(section::addStyle);
		FhirPanel.getStyles().forEach(section::addStyle);
		LinkCell.getStyles().forEach(section::addStyle);
		StructureDefinitionDetails.getStyles().forEach(section::addStyle);
		
		return section;
	}

	private Element getDetailsPanel() {
		StructureDefinitionTreeDataProvider dataProvider = new StructureDefinitionTreeDataProvider(wrappedResource, otherResources);
		FhirTreeData snapshotTreeData = dataProvider.getSnapshotTreeData();
		FhirTreeData differentialTreeData = dataProvider.getDifferentialTreeData(snapshotTreeData);
		
		snapshotTreeData.stripRemovedElements();
		new RedundantValueNodeRemover(differentialTreeData).process(snapshotTreeData);
		snapshotTreeData.tidyData();
		
		LinkedHashMap<String, StructureDefinitionDetails> details = Maps.newLinkedHashMap();
		
		for (FhirTreeTableContent node : snapshotTreeData) {
			FhirTreeNode fhirTreeNode = (FhirTreeNode)node;
			
			String pathName = fhirTreeNode.getPathName();
			String key = fhirTreeNode.getNodeKey();
			Optional<String> definition = fhirTreeNode.getDefinition();
			String cardinality = fhirTreeNode.getCardinality().toString();
			Optional<BindingInfo> binding = fhirTreeNode.getBinding();
			LinkDatas typeLinks = fhirTreeNode.getTypeLinks();
			Optional<String> requirements = fhirTreeNode.getRequirements();
			List<String> aliases = fhirTreeNode.getAliases();
			ResourceFlags resourceFlags = fhirTreeNode.getResourceFlags();
			Optional<String> comments = fhirTreeNode.getComments();
			Optional<String> linkedNodeKey = fhirTreeNode.getLinkedNode().isPresent() ? 
				Optional.of(fhirTreeNode.getLinkedNode().get().getPath()) : 
				Optional.empty();
			
			List<ConstraintInfo> inheritedConstraints = Lists.newArrayList();
			List<ConstraintInfo> profileConstraints = Lists.newArrayList();
			splitConstraints((FhirTreeNode)node, differentialTreeData, inheritedConstraints, profileConstraints);
			
			StructureDefinitionDetails detail = new StructureDefinitionDetails(pathName, key, definition, cardinality, binding, typeLinks,
				requirements, aliases, resourceFlags, comments, node.getSlicingInfo(), inheritedConstraints, profileConstraints,
				linkedNodeKey, fhirTreeNode.getMappings(), wrappedResource.getImplicitFhirVersion());

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
					new CSSRule(CSSTag.FONT_SIZE, "12px"),
					new CSSRule(CSSTag.FONT_FAMILY, FhirFont.DETAILS_HEADER),
					new CSSRule(CSSTag.PADDING, "3px"),
					new CSSRule(CSSTag.VERTICAL_ALIGN, "top"),
					
					new CSSRule(CSSTag.LINE_HEIGHT, "1.4em"),
					new CSSRule(CSSTag.FONT_WEIGHT, "normal"),
					new CSSRule(CSSTag.TEXT_ALIGN, "left"),
					new CSSRule(CSSTag.BORDER_BOTTOM, "1px solid " + FhirColour.DETAILS_PAGE_DIVIDER),
					new CSSRule(CSSTag.BACKGROUND, FhirColour.DETAILS_HEADER_BACKGROUND))));
		
		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList("." + FhirCSS.DETAILS_DATA_ROW, "." + FhirCSS.DETAILS_DATA_CELL),
				Lists.newArrayList(
					new CSSRule(CSSTag.PADDING, "3px"),
					new CSSRule(CSSTag.FONT_FAMILY, FhirFont.DETAILS_DATA),
					new CSSRule(CSSTag.FONT_SIZE, "11px"),
					new CSSRule(CSSTag.LINE_HEIGHT, "1.2em"),
					new CSSRule(CSSTag.FONT_WEIGHT, "normal"),
					new CSSRule(CSSTag.VERTICAL_ALIGN, "top"),
					new CSSRule(CSSTag.TEXT_ALIGN, "left"))));
		
		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList("." + FhirCSS.DETAILS_HEADER_CELL),
				Lists.newArrayList(
					new CSSRule(CSSTag.FONT_SIZE, "12px"),
					new CSSRule(CSSTag.FONT_FAMILY, FhirFont.DETAILS_HEADER),
					new CSSRule(CSSTag.PADDING, "3px"),
					new CSSRule(CSSTag.VERTICAL_ALIGN, "top"),
					
					new CSSRule(CSSTag.LINE_HEIGHT, "1.2em"),
					new CSSRule(CSSTag.FONT_WEIGHT, "bold"),
					new CSSRule(CSSTag.BORDER_BOTTOM, "1px solid " + FhirColour.DETAILS_PAGE_DIVIDER),
					new CSSRule(CSSTag.BACKGROUND, FhirColour.DETAILS_HEADER_BACKGROUND))));
		
		return styles;
	}
}
