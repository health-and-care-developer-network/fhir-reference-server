package uk.nhs.fhir.render.format.structdef;

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
import uk.nhs.fhir.data.structdef.tree.DifferentialData;
import uk.nhs.fhir.data.structdef.tree.DifferentialTreeNode;
import uk.nhs.fhir.data.structdef.tree.FhirTreeData;
import uk.nhs.fhir.data.structdef.tree.SnapshotData;
import uk.nhs.fhir.data.structdef.tree.SnapshotTreeNode;
import uk.nhs.fhir.data.structdef.tree.tidy.ChildlessDummyNodeRemover;
import uk.nhs.fhir.data.structdef.tree.tidy.ComplexExtensionChildrenStripper;
import uk.nhs.fhir.data.structdef.tree.tidy.ExtensionsSlicingNodesRemover;
import uk.nhs.fhir.data.structdef.tree.tidy.RedundantValueNodeRemover;
import uk.nhs.fhir.data.structdef.tree.tidy.RemovedElementStripper;
import uk.nhs.fhir.data.structdef.tree.tidy.UnwantedConstraintRemover;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.render.RendererContext;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.ResourceFormatter;
import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.cell.LinkCell;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.style.CSSRule;
import uk.nhs.fhir.render.html.style.CSSStyleBlock;
import uk.nhs.fhir.render.html.style.CSSTag;
import uk.nhs.fhir.render.html.style.FhirCSS;
import uk.nhs.fhir.render.html.style.FhirColour;
import uk.nhs.fhir.render.html.style.FhirFont;
import uk.nhs.fhir.render.html.table.Table;

public class StructureDefinitionDetailsFormatter extends ResourceFormatter<WrappedStructureDefinition> {

	public StructureDefinitionDetailsFormatter(WrappedStructureDefinition wrappedResource) {
		super(wrappedResource);
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
		FhirTreeData<SnapshotData, SnapshotTreeNode> snapshotTreeData = wrappedResource.getSnapshotTree(Optional.of(RendererContext.forThread().getFhirFileRegistry()));
		FhirTreeData<DifferentialData, DifferentialTreeNode> differentialTreeData = wrappedResource.getDifferentialTree(Optional.of(RendererContext.forThread().getFhirFileRegistry()));
		
		new RemovedElementStripper<>(differentialTreeData).process();
		new ChildlessDummyNodeRemover<>(differentialTreeData).process();
		new RedundantValueNodeRemover<>(differentialTreeData).process(snapshotTreeData);
		new ExtensionsSlicingNodesRemover<>(snapshotTreeData).process();
		new UnwantedConstraintRemover<>(snapshotTreeData).process();
		new ComplexExtensionChildrenStripper<>(snapshotTreeData).process();
		
		LinkedHashMap<String, StructureDefinitionDetails> details = Maps.newLinkedHashMap();
		
		for (SnapshotTreeNode node : snapshotTreeData.nodes()) {
			
			SnapshotData nodeData = node.getData();
			
			String pathName = nodeData.getPath().getPathName();
			String key = node.getNodeKey();
			Optional<String> definition = nodeData.getDefinition();
			String cardinality = nodeData.getCardinality().toString();
			Optional<BindingInfo> binding = nodeData.getBinding();
			LinkDatas typeLinks = nodeData.getTypeLinks();
			Optional<String> requirements = nodeData.getRequirements();
			List<String> aliases = nodeData.getAliases();
			ResourceFlags resourceFlags = nodeData.getResourceFlags();
			Optional<String> comments = nodeData.getComments();
			Optional<String> linkedNodeKey = nodeData.getLinkedNode().isPresent() ? 
				Optional.of(nodeData.getLinkedNode().get().getPath().toPathString()) : 
				Optional.empty();
			
			List<ConstraintInfo> inheritedConstraints = Lists.newArrayList();
			List<ConstraintInfo> profileConstraints = Lists.newArrayList();
			splitConstraints(node, differentialTreeData, inheritedConstraints, profileConstraints);
			
			if (typeLinks.isEmpty()
			  && !nodeData.isRoot()) {
				throw new IllegalStateException("No typeLinks or linked Node present for non root node " + pathName);
			}
			
			StructureDefinitionDetails detail = new StructureDefinitionDetails(pathName, key, definition, cardinality, binding, typeLinks,
				requirements, aliases, resourceFlags, comments, nodeData.getSlicingInfo(), inheritedConstraints, profileConstraints,
				linkedNodeKey, nodeData.getMappings(), getResourceVersion());

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

	private void splitConstraints(SnapshotTreeNode node, FhirTreeData<DifferentialData, DifferentialTreeNode> differentialTreeData,
			List<ConstraintInfo> inheritedConstraints, List<ConstraintInfo> profileConstraints) {
		
		Optional<DifferentialData> matchingDifferentialNode = StreamSupport.stream(differentialTreeData.spliterator(), false)
			.filter(differentialNode -> differentialNode.getBackupNode().equals(node))
			.findFirst();
		
		if (matchingDifferentialNode.isPresent()) {
			List<ConstraintInfo> differentialConstraints = matchingDifferentialNode.get().getConstraints();
			
			for (ConstraintInfo constraint : node.getData().getConstraints()) {
				if (differentialConstraints.stream()
						.anyMatch(diffConstraint -> diffConstraint.getKey().equals(constraint.getKey()))) {
					profileConstraints.add(constraint);
				} else {
					inheritedConstraints.add(constraint);
				}
			}
		} else {
			// If not in the differential, it hasn't changed. Any constraints must be inherited.
			inheritedConstraints.addAll(node.getData().getConstraints());
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
