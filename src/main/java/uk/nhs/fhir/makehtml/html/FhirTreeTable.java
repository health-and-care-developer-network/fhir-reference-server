package uk.nhs.fhir.makehtml.html;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.nhs.fhir.makehtml.CSSStyleBlock;
import uk.nhs.fhir.makehtml.data.BindingResourceInfo;
import uk.nhs.fhir.makehtml.data.FhirIcon;
import uk.nhs.fhir.makehtml.data.FhirTreeData;
import uk.nhs.fhir.makehtml.data.FhirTreeNode;
import uk.nhs.fhir.makehtml.data.FhirTreeSlicingNode;
import uk.nhs.fhir.makehtml.data.ResourceInfo;
import uk.nhs.fhir.makehtml.data.ResourceInfoType;
import uk.nhs.fhir.makehtml.data.SlicingInfo;
import uk.nhs.fhir.util.TableTitle;

public class FhirTreeTable {
	private final FhirTreeData data;
	private final Style lineStyle = Style.DOTTED;
	
	public FhirTreeTable(FhirTreeData data) {
		this.data = data;
	}

	public Table asTable(boolean showRemoved) {
		return new Table(getColumns(), getRows(showRemoved), Sets.newHashSet());
	}
	
	private List<TableTitle> getColumns() {
		return Lists.newArrayList(
			new TableTitle("Name", "The logical name of the element", "30%"),
			new TableTitle("Flags", "Features of the element", "5%", "60px"),
			new TableTitle("Card.", "Minimum and maximum # of times the element can appear in the instance", "5%", "40px"),
			new TableTitle("Type", "Reference to the type of the element", "20%", "80px"),
			new TableTitle("Value", "Additional information about the element", "40%")
		);
	}
	
	private List<TableRow> getRows(boolean showRemoved) {
		List<TableRow> tableRows = Lists.newArrayList();
		
		if (!showRemoved) {
			stripRemovedElements(data.getRoot());
		}
		
		addSlicingNodes(data.getRoot());
		
		FhirTreeNode root = data.getRoot();
		root.getId().setFhirIcon(FhirIcon.RESOURCE);
		List<Boolean> rootVlines = Lists.newArrayList(root.hasChildren());
		List<FhirTreeIcon> rootIcons = Lists.newArrayList();
		
		addTableRow(tableRows, root, rootVlines, rootIcons);
		addNodeRows(root, tableRows, rootVlines);
		
		return tableRows;
	}

	private void addSlicingNodes(FhirTreeNode node) {
		List<FhirTreeNode> children = node.getChildren();
		
		// take care of any slicing within child nodes before we make any changes to the structure
		for (FhirTreeNode child : node.getChildren()) {
			addSlicingNodes(child);
		}
		
		// identify each range of children contained within a slicing block
		while (hasSlicing(children)) {
			int sliceStartIndex = -1;
			int sliceEndIndex = -1;
			
			// find the range of children incorporated in this slice
			FhirTreeNode nodeWithSlicing = null;
			for (int i=0; i<children.size(); i++) {
				FhirTreeNode child = children.get(i);
				
				Optional<SlicingInfo> slicingInfo = child.getSlicingInfo();
				if (slicingInfo.isPresent()) {
					
					nodeWithSlicing = child;
					
					String slicingPath = child.getPath();
					sliceStartIndex = i;
					
					// seek to end of children with matching paths
					sliceEndIndex = i;
					while (sliceEndIndex+1 < children.size()
					  && children.get(sliceEndIndex+1).getPath().equals(slicingPath)) {
						sliceEndIndex++;
					}
					
					break;
				}
			}
			
			// create a slice node for the tree to contain the child nodes
			FhirTreeNode slicingNode = new FhirTreeSlicingNode(nodeWithSlicing);
			
			// remove the child nodes from the parent node and add them to the slicing node
			for (int i=sliceEndIndex; i>=sliceStartIndex; i--) {
				FhirTreeNode slicedNode = children.remove(i);
				slicingNode.addChild(0, slicedNode);
			}
			
			// insert the slicing node into the place the children were
			children.add(sliceStartIndex, slicingNode);
			
			// remove the slicing information from the node it was on
			nodeWithSlicing.setSlicingInfo(null);
		}
	}

	private boolean hasSlicing(List<FhirTreeNode> children) {
		return children.stream()
				.anyMatch(fhirTreeNode -> 
					!(fhirTreeNode instanceof FhirTreeSlicingNode) 
					  && fhirTreeNode.getSlicingInfo().isPresent());
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
			
			if (childNode.hasChildren() && !(childNode instanceof FhirTreeSlicingNode)) {
				FhirIcon currentIcon = childNode.getId().getFhirIcon();
				// update default icon to folder icon
				if (currentIcon.equals(FhirIcon.DATATYPE)) {
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
				new ValueWithInfoCell(node.getInformation(), getNodeResourceInfos(node))));
	}
	
	private List<ResourceInfo> getNodeResourceInfos(FhirTreeNode node) {
		List<ResourceInfo> resourceInfos = Lists.newArrayList();
		
		// slicing
		if (node.getSlicingInfo().isPresent()) {
			Optional<ResourceInfo> slicingResourceInfo = node.getSlicingInfo().get().toResourceInfo();
			if (slicingResourceInfo.isPresent()) {
				resourceInfos.add(slicingResourceInfo.get());
			}
		}
		
		// slicing discriminator
		FhirTreeNode ancestor = node.getParent();
		while (ancestor != null) {
			if (ancestor.getSlicingInfo().isPresent()) {
				List<String> discriminatorPaths = ancestor.getSlicingInfo().get().getDiscriminatorPaths();
				String discriminatorPathRoot = ancestor.getPath() + ".";
				for (String discriminatorPath : discriminatorPaths) {
					if ((discriminatorPathRoot + discriminatorPath).equals(node.getPath())) {
						resourceInfos.add(new ResourceInfo("Slice discriminator", discriminatorPath, ResourceInfoType.SLICING_DISCRIMINATOR));
					}
				}
			}
			ancestor = ancestor.getParent();
		}
		
		// FixedValue
		if (node.isFixedValue()) {
			String description = node.getFixedValue().get();
			resourceInfos.add(makeResourceInfoWithMaybeUrl("Fixed Value", description, ResourceInfoType.FIXED_VALUE));
		}
		
		// Example
		if (node.hasExample()) {
			resourceInfos.add(makeResourceInfoWithMaybeUrl("Example Value", node.getExample().get(), ResourceInfoType.EXAMPLE_VALUE));
		}
		
		// Default Value
		if (node.hasDefaultValue()) {
			resourceInfos.add(makeResourceInfoWithMaybeUrl("Default Value", node.getDefaultValue().get(), ResourceInfoType.DEFAULT_VALUE));
		}
		
		// Binding
		if (node.hasBinding()) {
			resourceInfos.add(new BindingResourceInfo(node.getBinding().get()));
		}
		
		return resourceInfos;
	}

	private ResourceInfo makeResourceInfoWithMaybeUrl(String title, String value, ResourceInfoType type) {
		if (looksLikeUrl(value)) {
			try {
				return new ResourceInfo(title, new URL(value), type);
			} catch (MalformedURLException e) {
				// revert to non-link version
			}
		} 
		
		return new ResourceInfo(title, value, type);
	}

	private boolean looksLikeUrl(String description) {
		return description.startsWith("http://") || description.startsWith("https://");
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
	
	private boolean[] listToBoolArray(List<Boolean> bools) {
		boolean[] boolArray = new boolean[bools.size()];
		for (int i=0; i<bools.size(); i++) {
			boolArray[i] = bools.get(i);
		}
		return boolArray;
	}
	
	public List<CSSStyleBlock> getStyles() {
		List<CSSStyleBlock> tableStyles = Lists.newArrayList();

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
