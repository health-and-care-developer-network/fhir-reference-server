package uk.nhs.fhir.makehtml.html;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.nhs.fhir.makehtml.FhirVersion;
import uk.nhs.fhir.makehtml.UrlValidator;
import uk.nhs.fhir.makehtml.data.BindingInfo;
import uk.nhs.fhir.makehtml.data.ResourceInfo;
import uk.nhs.fhir.makehtml.data.ResourceInfoType;
import uk.nhs.fhir.makehtml.data.structdef.ConstraintInfo;
import uk.nhs.fhir.makehtml.data.structdef.tree.BindingResourceInfo;
import uk.nhs.fhir.makehtml.data.structdef.tree.FhirTreeData;
import uk.nhs.fhir.makehtml.data.structdef.tree.FhirTreeNode;
import uk.nhs.fhir.makehtml.data.structdef.tree.FhirTreeTableContent;
import uk.nhs.fhir.makehtml.data.url.FhirURL;
import uk.nhs.fhir.makehtml.data.url.LinkData;
import uk.nhs.fhir.makehtml.data.url.LinkDatas;
import uk.nhs.fhir.makehtml.html.style.CSSRule;
import uk.nhs.fhir.makehtml.html.style.CSSStyleBlock;

public class FhirTreeTable {
	
	private final FhirTreeData data;
	private final Style lineStyle = Style.DOTTED;
	private final FhirVersion version;
	
	private final FhirIconProvider icons = new FhirIconProvider();
	
	public FhirTreeTable(FhirTreeData data, FhirVersion version) {
		this.data = data;
		this.version = version;
	}
	
	public FhirTreeData getData() {
		return data;
	}

	public Table asTable() {
		return new Table(getColumns(), getRows(), Sets.newHashSet());
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
	
	private List<TableRow> getRows() {
		List<TableRow> tableRows = Lists.newArrayList();
		
		data.tidyData();
		
		FhirTreeTableContent root = data.getRoot();
		
		List<Boolean> rootVlines = Lists.newArrayList(root.hasChildren());
		List<FhirTreeIcon> rootIcons = Lists.newArrayList();
		
		addTableRow(tableRows, root, rootVlines, rootIcons);
		addNodeRows(root, tableRows, rootVlines);
		
		return tableRows;
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

			addTableRow(tableRows, childNode, childVlines, treeIcons);
			addNodeRows(childNode, tableRows, childVlines);
		}
	}
	
	private void addTableRow(List<TableRow> tableRows, FhirTreeTableContent nodeToAdd, List<Boolean> rootVlines, List<FhirTreeIcon> treeIcons) {
		boolean[] vlinesRequired = listToBoolArray(rootVlines);
		String backgroundCSSClass = TablePNGGenerator.getCSSClass(lineStyle, vlinesRequired);
		LinkDatas typeLinks = nodeToAdd.getTypeLinks();
		
		if (typeLinks.isEmpty()) {
			RendererError.handle(RendererError.Key.EMPTY_TYPE_LINKS, "No type links available for " + nodeToAdd.getPath());
		}
		
		boolean removedByProfile = nodeToAdd.isRemovedByProfile();
		
		tableRows.add(
			new TableRow(
				new TreeNodeCell(treeIcons, icons.getIcon(nodeToAdd), nodeToAdd.getDisplayName(), backgroundCSSClass, removedByProfile, nodeToAdd.getNodeKey(), nodeToAdd.getDefinition()),
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
			boolean maybeLogicalUrl = node.getPath().endsWith("coding.system")
			  || node.getPath().endsWith("identifier.system");
			
			if (maybeLogicalUrl
			  && looksLikeUrl(description)
			  && !new UrlValidator().testSingleUrl(description)) {
				resourceInfos.add(new ResourceInfo("Fixed Value", description, ResourceInfoType.FIXED_VALUE));
			} else {
				resourceInfos.add(makeResourceInfoWithMaybeUrl("Fixed Value", description, ResourceInfoType.FIXED_VALUE));
			}
		}
		
		// Examples
		for (String example : node.getExamples()) {
			if (!node.isFixedValue()) {
				resourceInfos.add(new ResourceInfo("Example Value", example, ResourceInfoType.EXAMPLE_VALUE));
			}
		}
		
		if (node.hasDefaultValue()
		  && node.isFixedValue()) {
			throw new IllegalStateException("Fixed value and default value");
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
				RendererError.handle(RendererError.Key.STAND_IN_BINDING_DESCRIPTION_NOT_REMOVED,
					"Stand-in description being displayed - expected this to have been removed by cardinality in profile");
			} else {
				resourceInfos.add(new BindingResourceInfo(bindingToAdd));
			}
		}
		
		// Extensions
		if (node.getPathName().equals("extension")) {
			LinkDatas typeLinks = node.getTypeLinks();
			for (Entry<LinkData, List<LinkData>> link : typeLinks.links()) {
				if (!link.getValue().isEmpty()
				  && link.getKey().getText().equals("Extension")) {
					for (LinkData nestedLink : link.getValue()) {
						resourceInfos.add(new ResourceInfo("URL", nestedLink.getURL(), ResourceInfoType.EXTENSION_URL));
					}
				}
			}
		}
		
		return resourceInfos;
	}

	private ResourceInfo makeResourceInfoWithMaybeUrl(String title, String value, ResourceInfoType type) {
		if (looksLikeUrl(value)) {
			try {
				return new ResourceInfo(title, FhirURL.buildOrThrow(value, version), type);
			} catch (IllegalStateException e) {
				// revert to non-link version
			}
		} 
		
		return new ResourceInfo(title, value, type);
	}

	private boolean looksLikeUrl(String description) {
		boolean hasScheme = description.startsWith("http://") || description.startsWith("https://");
		
		if (!hasScheme && description.contains("/")) {
			System.out.println("Should this be a link? " + description);
		}
		
		return hasScheme;
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
				Lists.newArrayList("." + FhirCSS.TREE_ICONS),
				Lists.newArrayList(
					new CSSRule("padding", "0 4px"),
					new CSSRule("border-collapse", "collapse"))));

		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList("." + FhirCSS.TREE_ICONS + " img"),
				Lists.newArrayList(
					new CSSRule("vertical-align", "top"),
					new CSSRule("float", "left"),
					new CSSRule("border-collapse", "collapse"))));

		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList("." + FhirCSS.TABLE, "." + FhirCSS.TABLE + " tbody tr"),
				Lists.newArrayList(
					new CSSRule("border-collapse", "collapse"),
					new CSSRule("vertical-align", "top"),
					new CSSRule("border-style", "none"))));
		
		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList("." + FhirCSS.TREE_ICONS, "." + FhirCSS.TREE_ICONS + " img", "." + FhirCSS.TABLE),
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
			new CSSStyleBlock(Lists.newArrayList("." + FhirCSS.TREE_RESOURCE_ICON),
				Lists.newArrayList(
					new CSSRule("padding-right", "4px"),
					new CSSRule("background-color", "white"),
					new CSSRule("border", "0"),
					new CSSRule("width", "20"),
					new CSSRule("height", "16"))));
		
		return iconStyles;
	}

	public void stripRemovedElements() {
		data.stripRemovedElements();
	}
}
