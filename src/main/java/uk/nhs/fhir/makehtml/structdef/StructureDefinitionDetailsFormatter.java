package uk.nhs.fhir.makehtml.structdef;

import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
import uk.nhs.fhir.makehtml.CSSStyleBlock;
import uk.nhs.fhir.makehtml.HTMLDocSection;
import uk.nhs.fhir.makehtml.ResourceFormatter;
import uk.nhs.fhir.makehtml.data.BindingInfo;
import uk.nhs.fhir.makehtml.data.ConstraintInfo;
import uk.nhs.fhir.makehtml.data.FhirTreeData;
import uk.nhs.fhir.makehtml.data.FhirTreeNode;
import uk.nhs.fhir.makehtml.data.FhirTreeTableContent;
import uk.nhs.fhir.makehtml.data.LinkData;
import uk.nhs.fhir.makehtml.data.ResourceFlags;
import uk.nhs.fhir.makehtml.html.CSSRule;
import uk.nhs.fhir.makehtml.html.FhirPanel;
import uk.nhs.fhir.makehtml.html.LinkCell;
import uk.nhs.fhir.makehtml.html.Table;
import uk.nhs.fhir.util.Elements;

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
			
			String key = getNodeKey(fhirTreeNode);
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
			
			StructureDefinitionDetails detail = new StructureDefinitionDetails(key, definition, cardinality, binding, typeLinks,
				requirements, aliases, resourceFlags, comments, inheritedConstraints, profileConstraints);

			if (!details.containsKey(key)) {
				details.put(key, detail);
			} else {
				StructureDefinitionDetails existingDetails = details.get(key);
				assertDetailsEqual(key, detail, existingDetails);
			}
		}
		
		List<Element> tableContent = Lists.newArrayList();
		for (StructureDefinitionDetails detail : details.values()) {
			detail.addContent(tableContent);
		}
		
		Element table = 
			Elements.withAttributeAndChildren("table",
				new Attribute("class", "fhir-table"),
				tableContent);
		
		FhirPanel panel = new FhirPanel("Details", table);
		
		return panel.makePanel();
	}

	void assertDetailsEqual(String key, StructureDefinitionDetails detail,
			StructureDefinitionDetails existingDetails) {
		if (!existingDetails.getDefinition().equals(detail.getDefinition())) {
			throw new IllegalStateException("Same key, different definition (" + key + ").");
		}
		if (!existingDetails.getCardinality().equals(detail.getCardinality())) {
			throw new IllegalStateException("Same key, different cardinality (" + key + ").");
		}
		if (!existingDetails.getBindingInfo().equals(detail.getBindingInfo())) {
			throw new IllegalStateException("Same key, different binding info (" + key + ").");
		}
		if (!existingDetails.getTypeLinks().stream().allMatch(link -> detail.getTypeLinks().contains(link))) {
			throw new IllegalStateException("Same key, different types info (" + key + ").");
		}
		if (!existingDetails.getRequirements().equals(detail.getRequirements())) {
			throw new IllegalStateException("Same key, different requirements info (" + key + ").");
		}
		if (!existingDetails.getAliases().stream().allMatch(alias -> detail.getAliases().contains(alias))) {
			throw new IllegalStateException("Same key, different alias info (" + key + ").");
		}
		if (!existingDetails.getResourceFlags().equals(detail.getResourceFlags())) {
			throw new IllegalStateException("Same key, different resource flags info (" + key + ").");
		}
		if (!existingDetails.getComments().equals(detail.getComments())) {
			throw new IllegalStateException("Same key, different comments info (" + key + ").");
		}
		if (!existingDetails.getInheritedConstraints().stream().allMatch(constraint -> detail.getInheritedConstraints().contains(constraint))) {
			throw new IllegalStateException("Same key, different inherited constraints info (" + key + ").");
		}
		if (!existingDetails.getProfileConstraints().stream().allMatch(constraint -> detail.getProfileConstraints().contains(constraint))) {
			throw new IllegalStateException("Same key, different profile constraints info (" + key + ").");
		}
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
	
	private String getNodeKey(FhirTreeNode node) {
		
		Deque<String> ancestorKeys = new LinkedList<>();
		
		FhirTreeNode ancestor = node;
		while (true) {
			ancestorKeys.addFirst(getKeySegment(ancestor));
			
			ancestor = (FhirTreeNode)ancestor.getParent();
			
			if (ancestor == null) {
				break;
			}
		}
		
		String key = String.join(".", ancestorKeys);
		
		/*Optional<String> fixed = node.getFixedValue();
		if (fixed.isPresent() 
		  && !fixed.get().isEmpty()) {
			key += ":" + fixed.get();
		}*/
		
		return key;
	}

	String getKeySegment(FhirTreeNode node) {
		String nodeKey = node.getPathName();
		
		Optional<String> name = node.getName();
		if (name.isPresent()
		  && !name.get().isEmpty()) {
			nodeKey += ":" + name.get();
		}
		
		return nodeKey;
	}
	
	public static List<CSSStyleBlock> getStyles() {
		List<CSSStyleBlock> styles = Lists.newArrayList();
		
		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList(".fhir-details-header-row"),
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
				Lists.newArrayList(".fhir-details-data-row", ".fhir-details-data-cell"),
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
				Lists.newArrayList(".fhir-details-header-cell"),
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
