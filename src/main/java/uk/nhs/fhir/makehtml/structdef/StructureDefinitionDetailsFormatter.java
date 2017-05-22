package uk.nhs.fhir.makehtml.structdef;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Text;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import uk.nhs.fhir.makehtml.CSSStyleBlock;
import uk.nhs.fhir.makehtml.HTMLDocSection;
import uk.nhs.fhir.makehtml.NewMain;
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
import uk.nhs.fhir.util.StringUtil;

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
		
		Set<String> nodeKeys = Sets.newHashSet();

		List<Element> tableContent = Lists.newArrayList();
		
		for (FhirTreeTableContent node : snapshotTreeData) {
			FhirTreeNode fhirTreeNode = (FhirTreeNode)node;
			String key = getNodeKey(fhirTreeNode);
			
			if (!nodeKeys.contains(key)) {
				nodeKeys.add(key);
			} else if (NewMain.STRICT) {
				throw new IllegalStateException("Identical keys for 2 nodes (" + key + "). Add validation to check they have identical details");
			}
			
			tableContent.add(getHeaderRow(key));
			
			addDataIfPresent(tableContent, "Definition", fhirTreeNode.getDefinition());
			addData(tableContent, "Cardinality", fhirTreeNode.getCardinality().toString());
			addBindingRowIfPresent(tableContent, fhirTreeNode.getBinding());
			tableContent.add(getLinkRow("Type", fhirTreeNode.getTypeLinks()));
			addDataIfPresent(tableContent, "Requirements", fhirTreeNode.getRequirements());
			addListDataIfPresent(tableContent, "Alternate Names", fhirTreeNode.getAliases());
			addResourceFlags(tableContent, fhirTreeNode.getResourceFlags());
			addDataIfPresent(tableContent, "Comments", fhirTreeNode.getComments());
			addConstraints(tableContent, fhirTreeNode, differentialTreeData);
		}
		
		Element table = 
			Elements.withAttributeAndChildren("table",
				new Attribute("class", "fhir-table"),
				tableContent);
		
		FhirPanel panel = new FhirPanel("Details", table);
		
		return panel.makePanel();
	}

	private void addConstraints(List<Element> tableContent, FhirTreeNode node, FhirTreeData differentialTreeData) {
		
		List<ConstraintInfo> constraints = node.getConstraints();
		
		//validate for duplicate keys
		for (int i=0; i<constraints.size(); i++) {
			ConstraintInfo constraint1 = constraints.get(i);
			for (int j=i+1; j<constraints.size(); j++) {
				ConstraintInfo constraint2 = constraints.get(j);
				if (constraint1.getKey().equals(constraint2.getKey())) {
					String warning = "Node with constraints with duplicate key: '" + constraint1.getKey() + "'";
					if (NewMain.STRICT) {
						throw new IllegalStateException(warning);
					} else {
						System.out.println("***" + warning + "***");
					}
				}
			}
		}
		
		if (!constraints.isEmpty()) {
			Element labelCell = dataCell("Invariants", "fhir-details-data-cell");

			List<ConstraintInfo> inheritedConstraints = Lists.newArrayList();
			List<ConstraintInfo> profileConstraints = Lists.newArrayList();
			splitConstraints(node, differentialTreeData, inheritedConstraints, profileConstraints);
			
			List<Content> constraintInfos = Lists.newArrayList();
			addConstraintInfos(constraintInfos, profileConstraints, "Defined on this element");
			addConstraintInfos(constraintInfos, inheritedConstraints, "Affect this element");
			
			tableContent.add(
				getDataRow(
					labelCell, 
					Elements.withAttributeAndChildren("td", 
						new Attribute("class", "fhir-details-data-cell"), 
						constraintInfos)));
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

	private void addConstraintInfos(List<Content> constraintInfos, List<ConstraintInfo> profileConstraints, String label) {
		if (!profileConstraints.isEmpty()) {
			if (!constraintInfos.isEmpty()) {
				constraintInfos.add(Elements.newElement("br"));
			}
			constraintInfos.add(Elements.withText("b", label));
			
			for (ConstraintInfo constraint : profileConstraints) {
				constraintInfos.add(Elements.newElement("br"));
				
				constraintInfos.add(Elements.withText("b", constraint.getKey() + ": "));
				String constraintContent = constraint.getDescription();
				if (constraint.getRequirements().isPresent()) {
					constraintContent += ". " + constraint.getRequirements().get();
				}
				constraintContent += " (xpath: " + constraint.getXPath() + ")";
				constraintContent += " severity: " + constraint.getSeverity();
				constraintInfos.add(new Text(constraintContent));
			}
		}
	}

	private void addBindingRowIfPresent(List<Element> tableContent, Optional<BindingInfo> binding) {
		if (binding.isPresent()) {
			BindingInfo info = binding.get();

			String bindingInfo = "";
			
			boolean hasUrl = info.getUrl().isPresent();
			boolean hasDesc = info.getDescription().isPresent();
			
			if (hasUrl) {
				String fullUrl = info.getUrl().get().toString();
				String hyphenatedUrlName = fullUrl.substring(fullUrl.lastIndexOf('/') + 1);
				String urlName = StringUtil.hyphenatedToPascalCase(hyphenatedUrlName);
				bindingInfo += urlName;
			}
			
			if (hasUrl && hasDesc) {
				bindingInfo += ": ";
			}
			
			if (hasDesc) {
				bindingInfo += info.getDescription().get();
			}
			
			bindingInfo += " (" + info.getStrength() + ")";
			
			addData(tableContent, "Binding", bindingInfo);
		}
	}

	private void addListDataIfPresent(List<Element> tableContent, String label, Optional<List<String>> listData) {
		if (listData.isPresent()) {
			addData(tableContent, label, String.join("; ", listData.get()));
		}
	}

	private void addResourceFlags(List<Element> tableContent, ResourceFlags resourceFlags) {
		addDataIfTrue(tableContent, "Summary", resourceFlags.isSummary());
		addDataIfTrue(tableContent, "Modifier", resourceFlags.isModifier());
		//addDataIfTrue(tableContent, "Is Constrained", resourceFlags.isConstrained());
		addDataIfTrue(tableContent, "Must-Support", resourceFlags.isMustSupport());
	}

	private void addDataIfTrue(List<Element> tableContent, String label, boolean condition) {
		if (condition) {
			addData(tableContent, label, "True");
		}
	}

	private void addDataIfPresent(List<Element> tableContent, String label, Optional<String> content) {
		if (content.isPresent()) {
			addData(tableContent, label, content.get());
		}
	}

	private void addData(List<Element> tableContent, String label, String content) {
		tableContent.add(simpleStringDataRow(label, content)); 
	}

	private Element getHeaderRow(String header) {
		return Elements.withAttributeAndChild("tr", 
			new Attribute("class", "fhir-details-header-row"), 
			Elements.withAttributesAndChildren("td",
				Lists.newArrayList(
					new Attribute("class", "fhir-details-header-cell"), 
					new Attribute("colspan", "2")), 
				Lists.newArrayList(
					Elements.withAttribute("a", 
						new Attribute("name", header)),
					new Text(header))));
	}
	
	private Element simpleStringDataRow(String title, String content) {
		
		Element labelCell = dataCell(title, "fhir-details-data-cell");
		Element dataCell = dataCell(content, "fhir-details-data-cell");
		
		return getDataRow(labelCell, dataCell);
	}
	
	private Element getDataRow(Element labelCell, Element dataCell) {
		return Elements.withAttributeAndChildren("tr", 
			new Attribute("class", "fhir-details-data-row"),
				Lists.newArrayList(
					labelCell,
					dataCell));
	}
	
	private Element dataCell(String content, String classString) {
		return Elements.withAttributeAndText("td",
			new Attribute("class", classString),
			content);
	}
	
	private Element getLinkRow(String title, List<LinkData> linkDatas) {
		return Elements.withAttributeAndChildren("tr", 
			new Attribute("class", "fhir-details-data-row"),
				Lists.newArrayList(
					dataCell(title, "fhir-details-data-cell"),
					linkCell(linkDatas)));
	}
	
	private Element linkCell(List<LinkData> linkDatas) {
		return new LinkCell(linkDatas, Sets.newHashSet("fhir-details-data-cell"), Sets.newHashSet(), false, false).makeCell();
	}
	
	private String getNodeKey(FhirTreeNode node) {
		String key = node.getPath();
		
		Optional<String> name = node.getName();
		if (name.isPresent()
		  && !name.get().isEmpty()) {
			key += ":" + name.get();
		}
		
		return key;
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
