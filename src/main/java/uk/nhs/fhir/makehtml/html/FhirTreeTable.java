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
import uk.nhs.fhir.makehtml.NewMain;
import uk.nhs.fhir.makehtml.data.BindingInfo;
import uk.nhs.fhir.makehtml.data.BindingResourceInfo;
import uk.nhs.fhir.makehtml.data.ConstraintInfo;
import uk.nhs.fhir.makehtml.data.DummyFhirTreeNode;
import uk.nhs.fhir.makehtml.data.FhirIcon;
import uk.nhs.fhir.makehtml.data.FhirTreeData;
import uk.nhs.fhir.makehtml.data.FhirTreeNode;
import uk.nhs.fhir.makehtml.data.FhirTreeTableContent;
import uk.nhs.fhir.makehtml.data.LinkData;
import uk.nhs.fhir.makehtml.data.NestedLinkData;
import uk.nhs.fhir.makehtml.data.ResourceInfo;
import uk.nhs.fhir.makehtml.data.ResourceInfoType;
import uk.nhs.fhir.makehtml.data.SimpleLinkData;
import uk.nhs.fhir.makehtml.data.UnchangedSliceInfoRemover;
import uk.nhs.fhir.util.TableTitle;

public class FhirTreeTable {
	// Hide the slicing of extensions
	private static final boolean dropExtensionSlicingNodes = true;
	
	private final FhirTreeData data;
	private final Style lineStyle = Style.DOTTED;
	
	public FhirTreeTable(FhirTreeData data) {
		this.data = data;
	}
	
	public FhirTreeData getData() {
		return data;
	}

	public Table asTable(boolean showRemoved, Optional<FhirTreeData> differential) {
		return new Table(getColumns(), getRows(showRemoved, differential), Sets.newHashSet());
	}
	
	private List<TableTitle> getColumns() {
		return Lists.newArrayList(
			new TableTitle("Name", "The logical name of the element", "30%"),
			new TableTitle("Flags", "Features of the element", "5%", "60px"),
			new TableTitle("Card.", "Minimum and maximum # of times the element can appear in the instance", "5%", "40px"),
			new TableTitle("Type", "Reference to the type of the element", "20%", "80px"),
			new TableTitle("Description/Constraints", "Additional information about the element", "40%")
		);
	}
	
	private List<TableRow> getRows(boolean showRemoved, Optional<FhirTreeData> differential) {
		List<TableRow> tableRows = Lists.newArrayList();
		
		if (!showRemoved) {
			data.stripRemovedElements();
		}
		
		if (dropExtensionSlicingNodes) {
			removeExtensionsSlicingNodes(data.getRoot());
		}
		
		if (differential.isPresent()) {
			UnchangedSliceInfoRemover remover = new UnchangedSliceInfoRemover(differential.get());
			remover.process(data);
		}
		
		stripChildlessDummyNodes(data.getRoot());
		
		addSlicingIcons(data.getRoot());
		
		FhirTreeTableContent root = data.getRoot();
		
		// Dummy nodes don't store icon info. If it is a dummy node, it will inherit the correct icon anyway.
		if (root instanceof FhirTreeData) {
			root.setFhirIcon(FhirIcon.RESOURCE);
		}
		List<Boolean> rootVlines = Lists.newArrayList(root.hasChildren());
		List<FhirTreeIcon> rootIcons = Lists.newArrayList();
		
		addTableRow(tableRows, root, rootVlines, rootIcons);
		addNodeRows(root, tableRows, rootVlines);
		
		return tableRows;
	}

	private void stripChildlessDummyNodes(FhirTreeTableContent node) {
		for (int i=node.getChildren().size()-1; i>=0; i--) {
			FhirTreeTableContent child = node.getChildren().get(i);
			
			stripChildlessDummyNodes(child);
			
			if (child instanceof DummyFhirTreeNode
			  && child.getChildren().isEmpty()) {
				node.getChildren().remove(i);
			}
		}
	}

	private void addSlicingIcons(FhirTreeTableContent node) {
		if (node.hasSlicingInfo()) {
			if (node instanceof FhirTreeNode) {
				FhirTreeNode fhirTreeNode = (FhirTreeNode)node;
				fhirTreeNode.setFhirIcon(FhirIcon.SLICE);
			} else {
				throw new IllegalStateException("Dummy node with slicing info");
			}
		}
		
		for (FhirTreeTableContent child : node.getChildren()) {
			// call recursively over whole tree
			addSlicingIcons(child);
		}
	}

	private void removeExtensionsSlicingNodes(FhirTreeTableContent node) {
		List<? extends FhirTreeTableContent> children = node.getChildren();
		
		// if there is an extensions slicing node (immediately under root), remove it.
		for (int i=children.size()-1; i>=0; i--) {
			FhirTreeTableContent child = children.get(i);
			if (child.getPathName().equals("extension")
			  && child.hasSlicingInfo()) {
				children.remove(i);
			} else {
				// call recursively over whole tree
				removeExtensionsSlicingNodes(child);
			}
		}
	}

	private void addNodeRows(FhirTreeTableContent node, List<TableRow> tableRows, List<Boolean> vlines) {
		
		List<? extends FhirTreeTableContent> children = node.getChildren();
		for (int i=0; i<children.size(); i++) {
			FhirTreeTableContent childNode = children.get(i);
			
			List<Boolean> childVlines = Lists.newArrayList(vlines);
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
			
			if (childNode.hasChildren()
			  && childNode instanceof FhirTreeNode) {
				FhirTreeNode fhirTreeNode = (FhirTreeNode)childNode;
				FhirIcon currentIcon = fhirTreeNode.getFhirIcon();
				// update default icon to folder icon
				if (currentIcon.equals(FhirIcon.DATATYPE)) {
					fhirTreeNode.setFhirIcon(FhirIcon.ELEMENT);
				}
			}

			addTableRow(tableRows, childNode, childVlines, treeIcons);
			addNodeRows(childNode, tableRows, childVlines);
		}
	}
	
	private void addTableRow(List<TableRow> tableRows, FhirTreeTableContent nodeToAdd, List<Boolean> rootVlines, List<FhirTreeIcon> treeIcons) {
		boolean[] vlinesRequired = listToBoolArray(rootVlines);
		String backgroundCSSClass = TablePNGGenerator.getCSSClass(lineStyle, vlinesRequired);
		List<LinkData> typeLinks = nodeToAdd.getTypeLinks();
		if (NewMain.STRICT && typeLinks.isEmpty()) {
			throw new IllegalStateException("No type links available for " + nodeToAdd.getPath());
		} else {
			System.out.println("No type links available for " + nodeToAdd.getPath());
		}
		
		boolean removedByProfile = nodeToAdd.isRemovedByProfile();
		
		tableRows.add(
			new TableRow(
				new TreeNodeCell(treeIcons, nodeToAdd.getFhirIcon(), nodeToAdd.getDisplayName(), backgroundCSSClass, removedByProfile),
				new ResourceFlagsCell(nodeToAdd.getResourceFlags()),
				new SimpleTextCell(nodeToAdd.getCardinality().toString(), nodeToAdd.useBackupCardinality(), removedByProfile), 
				new LinkCell(typeLinks, nodeToAdd.useBackupTypeLinks(), removedByProfile), 
				new ValueWithInfoCell(nodeToAdd.getInformation(), getNodeResourceInfos(nodeToAdd))));
	}
	
	private List<ResourceInfo> getNodeResourceInfos(FhirTreeTableContent node) {
		List<ResourceInfo> resourceInfos = Lists.newArrayList();
		
		for (ConstraintInfo constraint : node.getConstraints()) {
			resourceInfos.add(new ResourceInfo(constraint.getKey(), constraint.getDescription(), ResourceInfoType.CONSTRAINT));
		}
		
		// slicing
		if (node.hasSlicingInfo()) {
			Optional<ResourceInfo> slicingResourceInfo = node.getSlicingInfo().get().toResourceInfo();
			if (slicingResourceInfo.isPresent()) {
				resourceInfos.add(slicingResourceInfo.get());
			}
		}
		
		// slicing discriminator
		FhirTreeTableContent ancestor = node.getParent();
		while (ancestor != null) {
			if (ancestor.hasSlicingInfo()) {
				Set<String> discriminatorPaths = ancestor.getSlicingInfo().get().getDiscriminatorPaths();
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
			// never display as an actual link
			resourceInfos.add(new ResourceInfo("Example Value", node.getExample().get(), ResourceInfoType.EXAMPLE_VALUE));
		}
		
		if (node.hasDefaultValue()
		  && node.isFixedValue()) {
			throw new IllegalStateException("Found and example");
		}
		
		// Default Value
		if (node.hasDefaultValue()
		  && !node.isFixedValue()) {
			resourceInfos.add(makeResourceInfoWithMaybeUrl("Default Value", node.getDefaultValue().get(), ResourceInfoType.DEFAULT_VALUE));
		}
		
		// Binding
		if (node.hasBinding()) {
			BindingInfo childBinding = node.getBinding().get();
			BindingInfo bindingToAdd = childBinding;
			
			// Differential binding may only contain part of the data.
			// However, if it is present at all, it indicates a change, so should be displayed.
			if (node.hasBackupNode()) {
				FhirTreeNode backup = node.getBackupNode().get();
				if (backup.hasBinding()) {
					BindingInfo backupBinding = backup.getBinding().get();
					bindingToAdd = BindingInfo.resolveWithBackupData(childBinding, backupBinding);
				}
			}
			
			if (bindingToAdd.getDescription().equals(BindingInfo.STAND_IN_DESCRIPTION)) {
				throw new IllegalStateException("Stand-in description being displayed - expected this to have been removed by cardinality in profile");
			} else {
				resourceInfos.add(new BindingResourceInfo(bindingToAdd));
			}
		}
		
		// Extensions
		if (node.getPathName().equals("extension")) {
			List<LinkData> typeLinks = node.getTypeLinks();
			for (LinkData link : typeLinks) {
				if (link instanceof NestedLinkData
				  && link.getPrimaryLinkData().getText().equals("Extension")) {
					NestedLinkData extensionLinkData = (NestedLinkData)link;
					for (SimpleLinkData nestedLink : extensionLinkData.getNestedLinks()) {
						try {
							resourceInfos.add(new ResourceInfo("URL", new URL(nestedLink.getURL()), ResourceInfoType.EXTENSION_URL));
						} catch (MalformedURLException e) {
							throw new IllegalStateException("Failed to create URL for extension node");
						}
					}
				}
			}
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
		
		/*for (FhirIcon icon : getIcons()) {
			iconStyles.add(
				new CSSStyleBlock(Lists.newArrayList("." + icon.getCSSClass()),
					Lists.newArrayList(
						new CSSRule("padding-right", "4px"),
						new CSSRule("background-color", "white"),
						new CSSRule("border", "0"),
						new CSSRule("content", icon.getAsDataUrl()),
						new CSSRule("width", "20"),
						new CSSRule("height", "16"))));
		}*/
		
		iconStyles.add(
			new CSSStyleBlock(Lists.newArrayList(".fhir-tree-resource-icon"),
				Lists.newArrayList(
					new CSSRule("padding-right", "4px"),
					new CSSRule("background-color", "white"),
					new CSSRule("border", "0"),
					new CSSRule("width", "20"),
					new CSSRule("height", "16"))));
		
		return iconStyles;
	}
}
