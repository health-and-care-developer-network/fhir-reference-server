package uk.nhs.fhir.makehtml.html;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.nhs.fhir.makehtml.CSSStyleBlock;
import uk.nhs.fhir.makehtml.data.FhirIcon;
import uk.nhs.fhir.makehtml.data.FhirTreeData;
import uk.nhs.fhir.makehtml.data.FhirTreeNode;
import uk.nhs.fhir.util.TableTitle;

public class FhirTreeTable {
	private final FhirTreeData data;
	private final Style lineStyle = Style.DOTTED;
	
	private final TablePNGGenerator backgrounds = new TablePNGGenerator();
	
	public FhirTreeTable(FhirTreeData data) {
		this.data = data;
	}

	public Table asTable(boolean showRemoved) {
		return new Table(getColumns(), getRows(showRemoved), Sets.newHashSet());
	}
	
	private List<TableTitle> getColumns() {
		return Lists.newArrayList(
			new TableTitle("Name", "The logical name of the element", "120px"),
			new TableTitle("Flags", "Features of the element", "60px"),
			new TableTitle("Card.", "Minimum and maximum # of times the element can appear in the instance", "40px"),
			new TableTitle("Type", "Reference to the type of the element", "100px"),
			new TableTitle("Value", "Additional information about the element", "290px")
		);
	}
	
	private List<TableRow> getRows(boolean showRemoved) {
		List<TableRow> tableRows = Lists.newArrayList();
		
		if (!showRemoved) {
			stripRemovedElements(data.getRoot());
		}
		
		FhirTreeNode root = data.getRoot();
		root.getId().setFhirIcon(FhirIcon.RESOURCE);
		List<Boolean> rootVlines = Lists.newArrayList(root.hasChildren());
		List<FhirTreeIcon> rootIcons = Lists.newArrayList();
		
		addTableRow(tableRows, root, rootVlines, rootIcons);
		addNodeRows(root, tableRows, rootVlines);
		
		return tableRows;
	}

	/**
	 * Remove all nodes that have cardinality max = 0 (and their children)
	 */
	private void stripRemovedElements(FhirTreeNode node) {
		List<FhirTreeNode> children = node.getChildren();
		
		for (int i=children.size()-1; i>=0; i--) {
			
			FhirTreeNode child = children.get(i);
			
			if (child.isRemovedByProfile()) {
				children.remove(i);
			} else {
				stripRemovedElements(child);
			}
		}
	}

	private void addNodeRows(FhirTreeNode node, List<TableRow> tableRows, List<Boolean> vlines) {
		
		List<FhirTreeNode> children = node.getChildren();
		for (int i=0; i<children.size(); i++) {
			FhirTreeNode childNode = children.get(i);
			List<Boolean> childVlines = null;
			
			childVlines = Lists.newArrayList(vlines);
			childVlines.add(childNode.hasChildren());
			
			boolean lastChild = (i == children.size() - 1);
			if (lastChild) {
				childVlines.set(childVlines.size()-2, Boolean.FALSE);
			}
			
			ArrayList<FhirTreeIcon> treeIcons = Lists.newArrayList();
			for (int j=0; j<vlines.size(); j++) {
				boolean lineBelow = childVlines.get(j);
				boolean lastIcon = (j == vlines.size() - 1);
				
				if (lineBelow && !lastIcon) {
					treeIcons.add(FhirTreeIcon.VLINE);
				} else if (lineBelow && lastIcon) {
					treeIcons.add(FhirTreeIcon.VJOIN);
				} else if (!lineBelow && lastIcon) {
					treeIcons.add(FhirTreeIcon.VJOIN_END);
				} else if (!lineBelow && !lastIcon) {
					treeIcons.add(FhirTreeIcon.BLANK);
				}
			}
			
			if (childNode.hasChildren()) {
				FhirIcon currentIcon = childNode.getId().getFhirIcon();
				if (!currentIcon.equals(FhirIcon.SLICE)) {
					childNode.getId().setFhirIcon(FhirIcon.ELEMENT);
				}
			}

			addTableRow(tableRows, childNode, childVlines, treeIcons);
			addNodeRows(childNode, tableRows, childVlines);
		}
	}
	
	private void addTableRow(List<TableRow> tableRows, FhirTreeNode node, List<Boolean> rootVlines, List<FhirTreeIcon> treeIcons) {
		boolean[] vlinesRequired = listToBoolArray(rootVlines);
		String backgroundCSSClass = TablePNGGenerator.getCSSClass(lineStyle, vlinesRequired);
		tableRows.add(
				new TableRow(
					new TreeNodeCell(treeIcons, node.getId().getFhirIcon(), node.getId().getName(), backgroundCSSClass),
					new ResourceFlagsCell(node.getResourceFlags()),
					new SimpleTextCell(node.getCardinality().toString()), 
					new LinkCell(node.getTypeLink()), 
					new SimpleTextCell(node.getInformation())));
	}

	public Set<FhirIcon> getIcons() {
		Set<FhirIcon> icons = Sets.newHashSet();
		addIcons(data.getRoot(), icons);
		return icons;
	}
	
	public void addIcons(FhirTreeNode node, Set<FhirIcon> icons) {
		icons.add(node.getId().getFhirIcon());
		
		for (FhirTreeNode child : node.getChildren()) {
			addIcons(child, icons);
		}
	}
	
	/**
	 * Inspect each node and establish
	 */
	public Set<String> getBackgroundKeys() {
		Set<String> backgroundKeys = Sets.newHashSet();
		FhirTreeNode root = data.getRoot();
		addBackgroundKeys(root, backgroundKeys, Lists.newArrayList(true));
		
		return backgroundKeys;
	}

	private void addBackgroundKeys(FhirTreeNode node, Set<String> backgroundKeys, List<Boolean> vlines) {
		String backgroundKey = TablePNGGenerator.getCSSClass(lineStyle, listToBoolArray(vlines));
		backgroundKeys.add(backgroundKey);
		
		List<FhirTreeNode> children = node.getChildren();
		for (int i=0; i<children.size(); i++){
			List<Boolean> childVlines = Lists.newArrayList(vlines);
			
			boolean lastChild = (i == children.size() - 1);
			childVlines.add(!lastChild);
			
			addBackgroundKeys(children.get(i), backgroundKeys, childVlines);
		}
	}
	
	private boolean[] listToBoolArray(List<Boolean> bools) {
		boolean[] boolArray = new boolean[bools.size()];
		for (int i=0; i<bools.size(); i++) {
			boolArray[i] = bools.get(i);
		}
		return boolArray;
	}
	
	public List<CSSStyleBlock> getStyles() {
		List<CSSStyleBlock> tableStyles = Lists.newArrayList();

		tableStyles.addAll(getBackgroundStyles());
		tableStyles.addAll(getIconStyles());
		tableStyles.addAll(FhirTreeIcon.getCssRules());
		tableStyles.addAll(getLayoutStyles());
		
		return tableStyles;
	}
	
	private List<CSSStyleBlock> getLayoutStyles() {
		List<CSSStyleBlock> styles = Lists.newArrayList();
		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList(".fhir-tree-icons"),
				Lists.newArrayList(
					new CSSRule("padding", "0 4px"),
					new CSSRule("border-collapse", "collapse"))));

		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList(".fhir-tree-icons img"),
				Lists.newArrayList(
					new CSSRule("vertical-align", "top"),
					new CSSRule("float", "left"),
					new CSSRule("border-collapse", "collapse"))));

		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList(".fhir-table", ".fhir-table tbody tr"),
				Lists.newArrayList(
					new CSSRule("border-collapse", "collapse"),
					new CSSRule("vertical-align", "top"),
					new CSSRule("border-style", "none"))));
		
		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList(".fhir-tree-icons", ".fhir-tree-icons img", ".fhir-table"),
				Lists.newArrayList(
					new CSSRule("-webkit-border-horizontal-spacing", "0"),
					new CSSRule("-webkit-border-vertical-spacing", "0"))));
		
		return styles;
	}

	private List<CSSStyleBlock> getBackgroundStyles() {
		
		List<CSSStyleBlock> backgroundStyles = Lists.newArrayList();
		
		Set<String> backgroundKeys = getBackgroundKeys();
		for (String key : backgroundKeys) {
			String backgroundBase64 = backgrounds.getBase64(key);
			backgroundStyles.add(
				new CSSStyleBlock(Lists.newArrayList("." + key),
					Lists.newArrayList(
						new CSSRule("background-image", "url(data:image/png;base64," + backgroundBase64 + ")"),
						new CSSRule("background-repeat", "repeat-y"))));
		}
		
		return backgroundStyles;
	}

	private List<CSSStyleBlock> getIconStyles() {
		List<CSSStyleBlock> iconStyles = Lists.newArrayList();
		
		for (FhirIcon icon : getIcons()) {
			iconStyles.add(
				new CSSStyleBlock(Lists.newArrayList("." + icon.getCSSClass()),
					Lists.newArrayList(
						new CSSRule("padding-right", "4px"),
						new CSSRule("background-color", "white"),
						new CSSRule("border", "0"),
						new CSSRule("content", icon.getAsDataUrl()),
						new CSSRule("width", "20"),
						new CSSRule("height", "16"))));
		}
		
		return iconStyles;
	}
}
