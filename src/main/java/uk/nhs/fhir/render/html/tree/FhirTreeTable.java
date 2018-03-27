package uk.nhs.fhir.render.html.tree;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.ResourceInfo;
import uk.nhs.fhir.data.ResourceInfoType;
import uk.nhs.fhir.data.structdef.BindingInfo;
import uk.nhs.fhir.data.structdef.ConstraintInfo;
import uk.nhs.fhir.data.structdef.tree.AbstractFhirTreeNode;
import uk.nhs.fhir.data.structdef.tree.AbstractFhirTreeNodeData;
import uk.nhs.fhir.data.structdef.tree.BindingResourceInfo;
import uk.nhs.fhir.data.structdef.tree.FhirTreeData;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.LinkData;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;
import uk.nhs.fhir.render.html.cell.LinkCell;
import uk.nhs.fhir.render.html.cell.ResourceFlagsCell;
import uk.nhs.fhir.render.html.cell.SimpleTextCell;
import uk.nhs.fhir.render.html.cell.TableCell;
import uk.nhs.fhir.render.html.cell.TreeNodeCell;
import uk.nhs.fhir.render.html.cell.ValueWithInfoCell;
import uk.nhs.fhir.render.html.style.CSSRule;
import uk.nhs.fhir.render.html.style.CSSStyleBlock;
import uk.nhs.fhir.render.html.style.CSSTag;
import uk.nhs.fhir.render.html.style.FhirCSS;
import uk.nhs.fhir.render.html.table.Table;
import uk.nhs.fhir.render.html.table.TableRow;
import uk.nhs.fhir.render.html.table.TableTitle;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.UrlValidator;

public class FhirTreeTable<T extends AbstractFhirTreeNodeData, U extends AbstractFhirTreeNode<T, U>> {
	
	private static final Logger LOG = LoggerFactory.getLogger(FhirTreeTable.class);
	
	private final FhirTreeData<T, U> data;
	private final Style lineStyle = uk.nhs.fhir.render.html.tree.Style.DOTTED;
	private final FhirVersion version;
	
	private final FhirIconProvider<T, U> icons = new FhirIconProvider<>();
	
	public FhirTreeTable(FhirTreeData<T, U> data, FhirVersion version) {
		this.data = data;
		this.version = version;
	}
	
	public FhirTreeData<T, U> getData() {
		return data;
	}

	public Table asTable() {
		return new Table(getColumns(), getRows(), Lists.newArrayList(FhirCSS.TREE));
	}
	
	private List<TableTitle> getColumns() {
		return Lists.newArrayList(
			new TableTitle("Name", "The logical name of the element", "25%"),
			new TableTitle("Flags", "Features of the element", "5%", "60px"),
			new TableTitle("Card.", "Minimum and maximum # of times the element can appear in the instance", "5%", "40px"),
			new TableTitle("Type", "Reference to the type of the element", "15%", "80px"),
			new TableTitle("Description/Constraints", "Additional information about the element", "50%")
		);
	}
	
	private List<TableRow> getRows() {
		List<TableRow> tableRows = Lists.newArrayList();
		
		U root = data.getRoot();
		
		List<Boolean> rootVlines = Lists.newArrayList(root.hasChildren());
		List<FhirTreeIcon> rootIcons = Lists.newArrayList();
		
		addTableRow(tableRows, root, rootVlines, rootIcons, true);
		addChildrenRows(root, tableRows, rootVlines);
		
		return tableRows;
	}

	private void addChildrenRows(U node, List<TableRow> tableRows, List<Boolean> vlines) {
		
		List<U> children = node.getChildren();
		for (int i=0; i<children.size(); i++) {
			U childNode = children.get(i);
			
			List<Boolean> childVlines = Lists.newArrayList(vlines);
			childVlines.add(childNode.hasChildren());
			
			boolean lastChild = (i == children.size() - 1);
			if (lastChild) {
				childVlines.set(childVlines.size()-2, Boolean.FALSE);
			}
			
			List<FhirTreeIcon> treeIcons = Lists.newArrayList();
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
			addChildrenRows(childNode, tableRows, childVlines);
		}
	}

	private void addTableRow(List<TableRow> tableRows, U nodeToAdd, List<Boolean> rootVlines, List<FhirTreeIcon> treeIcons) {
		addTableRow(tableRows, nodeToAdd, rootVlines, treeIcons, false);
	}
	
	private void addTableRow(List<TableRow> tableRows, U nodeToAdd, List<Boolean> rootVlines, List<FhirTreeIcon> treeIcons, boolean isRoot) {
		boolean[] vlinesRequired = listToBoolArray(rootVlines);
		String backgroundCSSClass = TablePNGGenerator.getCSSClass(lineStyle, vlinesRequired);
		
		T nodeData = nodeToAdd.getData();
		
		LinkDatas typeLinks = nodeData.getTypeLinks();
		
		if (typeLinks.isEmpty()
		  && !isRoot) {
			EventHandlerContext.forThread().event(RendererEventType.EMPTY_TYPE_LINKS, "No type links available for " + nodeToAdd.getPath());
		}
		
		boolean removedByProfile = nodeToAdd.isRemovedByProfile();
		
		tableRows.add(
			new TableRow(
				new TreeNodeCell(treeIcons, icons.getIcon(nodeToAdd), nodeData.getDisplayName(), backgroundCSSClass, removedByProfile, nodeToAdd.getNodeKey(), nodeData.getDefinition()),
				new ResourceFlagsCell(nodeData.getResourceFlags()),
				isRoot ? 
					TableCell.empty() : 
					new SimpleTextCell(nodeData.getCardinality().toString(), false, nodeData.useBackupCardinality(), removedByProfile),
				new LinkCell(typeLinks, nodeData.useBackupTypeLinks(), removedByProfile, false),
				new ValueWithInfoCell(nodeData.getInformation(), getNodeResourceInfos(nodeToAdd))));
	}
	
	private List<ResourceInfo> getNodeResourceInfos(U node) {
		T nodeData = node.getData();
		
		List<ResourceInfo> resourceInfos = Lists.newArrayList();
		
		for (ConstraintInfo constraint : nodeData.getConstraints()) {
			ResourceInfo constraintResourceInfo = new ResourceInfo("Constraint", constraint.getDescription(), ResourceInfoType.CONSTRAINT);
			constraintResourceInfo.setQualifier("(" + constraint.getKey() + ")");
			resourceInfos.add(constraintResourceInfo);
		}
		
		// slicing
		if (nodeData.hasSlicingInfo()) {
			Optional<ResourceInfo> slicingResourceInfo = nodeData.getSlicingInfo().get().toResourceInfo();
			if (slicingResourceInfo.isPresent()) {
				resourceInfos.add(slicingResourceInfo.get());
			}
		}
		
		// slicing discriminator
		U ancestor = node.getParent();
		while (ancestor != null) {
			if (ancestor.getData().hasSlicingInfo()) {
				Set<String> discriminatorPaths = ancestor.getData().getSlicingInfo().get().getDiscriminatorPaths();
				String discriminatorPathRoot = ancestor.getPath() + ".";
				for (String discriminatorPath : discriminatorPaths) {
					if ((discriminatorPathRoot + discriminatorPath).equals(node.getPathString())) {
						resourceInfos.add(new ResourceInfo("Slice discriminator", discriminatorPath, ResourceInfoType.SLICING_DISCRIMINATOR));
					}
				}
			}
			ancestor = ancestor.getParent();
		}
		
		// FixedValue
		if (nodeData.isFixedValue()) {
			String description = nodeData.getFixedValue().get();
			boolean maybeLogicalUrl = node.getPathString().endsWith("coding.system")
			  || node.getPathString().endsWith("identifier.system");
			
			if (maybeLogicalUrl
			  && looksLikeUrl(description)
			  && !FhirURL.isLogicalUrl(description)
			  && !new UrlValidator().testSingleUrl(description)) {
				// textual link
				resourceInfos.add(new ResourceInfo("Fixed Value",  Optional.empty(), Optional.of(FhirURL.buildOrThrow(description, version)), 
						ResourceInfoType.FIXED_VALUE, true));
			} else {
				resourceInfos.add(makeResourceInfoWithMaybeUrl("Fixed Value", description, ResourceInfoType.FIXED_VALUE));
			}
		}
		
		// Examples
		for (String example : nodeData.getExamples()) {
			if (!nodeData.isFixedValue()) {
				resourceInfos.add(new ResourceInfo("Example Value", example, ResourceInfoType.EXAMPLE_VALUE));
			}
		}
		
		if (nodeData.hasDefaultValue()
		  && nodeData.isFixedValue()) {
			throw new IllegalStateException("Fixed value and default value");
		}
		
		// Default Value
		if (nodeData.hasDefaultValue()
		  && !nodeData.isFixedValue()) {
			resourceInfos.add(makeResourceInfoWithMaybeUrl("Default Value", nodeData.getDefaultValue().get(), ResourceInfoType.DEFAULT_VALUE));
		}
		
		// Binding
		if (nodeData.hasBinding()) {
			BindingInfo bindingToAdd = nodeData.getBinding().get();
			
			if (bindingToAdd.getDescription().map(description -> description.equals(BindingInfo.STAND_IN_DESCRIPTION)).orElse(Boolean.FALSE)) {
				EventHandlerContext.forThread().event(RendererEventType.STAND_IN_BINDING_DESCRIPTION_NOT_REMOVED,
					"Stand-in description being displayed - expected this to have been removed by cardinality in profile");
			} else {
				resourceInfos.add(new BindingResourceInfo(bindingToAdd));
			}
		}
		
		// Extensions
		if (nodeData.getPathName().equals("extension")) {
			LinkDatas typeLinks = nodeData.getTypeLinks();
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
		if (looksLikeUrl(value)
		  && !FhirURL.isLogicalUrl(value)) {
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
			LOG.warn("Should this be a link? " + description);
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

		tableStyles.add(getIconStyle());
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
					new CSSRule(CSSTag.PADDING, "0 4px"),
					new CSSRule(CSSTag.BORDER_COLLAPSE, "collapse"))));

		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList("." + FhirCSS.TREE_ICONS + " img"),
				Lists.newArrayList(
					new CSSRule(CSSTag.VERTICAL_ALIGN, "top"),
					new CSSRule(CSSTag.FLOAT, "left"),
					new CSSRule(CSSTag.BORDER_COLLAPSE, "collapse"))));

		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList("." + FhirCSS.TABLE, "." + FhirCSS.TABLE + " tbody tr"),
				Lists.newArrayList(
					new CSSRule(CSSTag.BORDER_COLLAPSE, "collapse"),
					new CSSRule(CSSTag.VERTICAL_ALIGN, "top"),
					new CSSRule(CSSTag.BORDER_STYLE, "none"))));

		styles.add(
			new CSSStyleBlock(
				Lists.newArrayList("." + FhirCSS.TREE_ICONS, "." + FhirCSS.TREE_ICONS + " img", "." + FhirCSS.TABLE),
				Lists.newArrayList(
					new CSSRule(CSSTag._WEBKIT_BORDER_HORIZONTAL_SPACING, "0"),
					new CSSRule(CSSTag._WEBKIT_BORDER_VERTICAL_SPACING, "0"))));
		
		styles.add(
				new CSSStyleBlock(
					Lists.newArrayList("." + FhirCSS.TREE_CELL),
					Lists.newArrayList(
						new CSSRule(CSSTag.PADDING, "5px 4px"))));
		
		return styles;
	}

	private CSSStyleBlock getIconStyle() {
		return new CSSStyleBlock(Lists.newArrayList("." + FhirCSS.TREE_RESOURCE_ICON),
				Lists.newArrayList(
					new CSSRule(CSSTag.PADDING_RIGHT, "4px"),
					new CSSRule(CSSTag.BACKGROUND_COLOR, "white"),
					new CSSRule(CSSTag.BORDER, "0"),
					new CSSRule(CSSTag.WIDTH, "20"),
					new CSSRule(CSSTag.HEIGHT, "16")));
	}
}
