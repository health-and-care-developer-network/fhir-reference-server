package uk.nhs.fhir.makehtml.data;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import ca.uhn.fhir.context.FhirDataTypes;
import uk.nhs.fhir.makehtml.html.RendererError;

public class FhirTreeNode implements FhirTreeTableContent {
	private FhirIcon icon;
	private final Optional<String> name;
	private final ResourceFlags resourceFlags;
	private final Optional<Integer> min;
	private final Optional<String> max;
	private final List<LinkData> typeLinks;
	private final String information;
	private final List<ConstraintInfo> constraints;
	private final String path;
	private final FhirDataType dataType;

	private Optional<ExtensionType> extensionType = Optional.empty();
	private Optional<SlicingInfo> slicingInfo = Optional.empty();
	private Optional<String> fixedValue = Optional.empty();
	private Optional<String> example = Optional.empty();
	private Optional<String> defaultValue = Optional.empty();
	private Optional<BindingInfo> binding = Optional.empty();
	private Optional<String> definition = Optional.empty();
	private Optional<String> requirements = Optional.empty();
	private Optional<String> comments = Optional.empty();
	private List<String> aliases = Lists.newArrayList();
	private Optional<String> linkedNodeName = Optional.empty();
	private Optional<FhirTreeTableContent> linkedNode = Optional.empty();
	private Optional<String> discriminatorValue = Optional.empty();
	private List<FhirElementMapping> mappings = Lists.newArrayList();

	private FhirTreeTableContent parent = null;
	private FhirTreeNode backupNode = null;

	private final List<FhirTreeTableContent> children = Lists.newArrayList();

	public FhirTreeNode(
			FhirIcon icon,
			Optional<String> name,
			ResourceFlags flags,
			Integer min,
			String max,
			List<LinkData> typeLinks,
			String information,
			List<ConstraintInfo> constraints,
			String path,
			FhirDataType dataType) {
		this.icon = icon;
		this.name = name;
		this.resourceFlags = flags;
		this.min = Optional.ofNullable(min);
		this.max = Optional.ofNullable(max);
		this.typeLinks = typeLinks;
		this.information = information;
		this.constraints = constraints;
		this.path = path;
		this.dataType = dataType;
	}
	
	/**
	 * Detailed description
	 */
	public Optional<String> getDefinition() {
		return definition;
	}

	public void setDefinition(Optional<String> definition) {
		this.definition = definition;
	}

	@Override
	public FhirIcon getFhirIcon() {
		// If using default and we have a backup, use the backup icon
		if (icon.equals(FhirIcon.ELEMENT)
		  && hasBackupNode()) {
			return backupNode.getFhirIcon();
		}

		return icon;
	}

	@Override
	public void setFhirIcon(FhirIcon icon) {
		this.icon = icon;
	}

	public Optional<String> getName() {
		return name;
	}

	public String getDisplayName() {
		boolean hasName = name.isPresent() && !name.get().isEmpty();
		String pathName = getPathName();
		boolean hasPath = !pathName.isEmpty();

		String displayName;
		if (hasName && hasPath && !pathName.equals(name.get())) {
			displayName = pathName + " (" + name.get() + ")";
		} else if (hasPath) {
			displayName = pathName;
		} else if (hasName) {
			displayName = name.get();
		} else {
			throw new IllegalStateException("No name or path information");
		}

		return displayName;
	}

	public boolean hasSlicingInfo() {
		return slicingInfo.isPresent();
	}

	public Optional<SlicingInfo> getSlicingInfo() {
		return slicingInfo;
	}

	public void setSlicingInfo(SlicingInfo slicingInfo) {
		this.slicingInfo = Optional.ofNullable(slicingInfo);
	}

	public Optional<Integer> getMin() {
		return min;
	}
	public Optional<String> getMax() {
		return max;
	}

	/*
	 * Return true if we have a backup node and we don't have any
	 * cardinality information.
	 */
	public boolean useBackupCardinality() {
		return (backupNode != null
				&& (!min.isPresent() && !max.isPresent()));
	}
	public FhirCardinality getCardinality() {
		if (min.isPresent() && max.isPresent()) {
			return new FhirCardinality(min.get(), max.get());
		} else {
			try {
				Integer resolvedMin = min.isPresent() ? min.get() : backupNode.getMin().get();
				String resolvedMax = max.isPresent() ? max.get() : backupNode.getMax().get();
				return new FhirCardinality(resolvedMin, resolvedMax);
			} catch (NullPointerException e) {
				if (backupNode == null) {
					RendererError.handle(RendererError.Key.MISSING_CARDINALITY, "Missing cardinality for " + getPath() + ": " + min + ".." + max, Optional.of(e));
					return new FhirCardinality(0, "*");
				} else {
					throw e;
				}
			}
		}
	}

	public ResourceFlags getResourceFlags() {
		return resourceFlags;
	}

	private static final Map<String, LinkData> fixableTypes = new HashMap<>();
	static {
		fixableTypes.put("extension", new SimpleLinkData(FullFhirURL.buildOrThrow("http://hl7.org/fhir/DSTU2/extensibility.html#Extension"), "Extension"));
	}
	
	public List<LinkData> getTypeLinks() {
		if (useBackupTypeLinks()) {
			return backupNode.getTypeLinks();
		}
		
		if (typeLinks.isEmpty()
		  && linkedNodeName.isPresent()) {
			String linkedContentKey = getLinkedNode().get().getNodeKey();
			typeLinks.add(new SimpleLinkData(FhirURL.buildOrThrow("details.html#" + linkedContentKey), "see " + linkedContentKey));
		}
		
		if (typeLinks.isEmpty()
		  && fixableTypes.containsKey(getPathName())) {
			RendererError.handle(RendererError.Key.FIX_MISSING_TYPE_LINK, "Filling in type link for " + getPath());
			typeLinks.add(fixableTypes.get(getPathName()));
		} 

		if (typeLinks.isEmpty()) {
			RendererError.handle(RendererError.Key.MISSING_TYPE_LINK, "Couldn't find any typelinks for " + path);
		}
		
		if (getPathName().endsWith("[x]")
		  && hasAllTypes()) {
			return Lists.newArrayList(FhirDataTypes.openTypeLink());
		} else {
			return typeLinks;
		}
	}
	
	private boolean hasAllTypes() {
		Set<String> allTypes = Sets.newHashSet("Boolean", "Integer", "Decimal", "base64Binary", "Instant", 
				"String", "Uri", "Date", "dateTime", "Time", "Code", "Oid", "Id", "unsignedInt", "positiveInt",
				"Markdown", "Annotation", "Attachment", "Identifier", "CodeableConcept", "Coding", "Quantity",
				"Range", "Period", "Ratio", "SampledData", "Signature", "HumanName", "Address", "ContactPoint",
				"Timing", "Reference", "Meta");
		
		Set<String> containedTypes = typeLinks.stream().map(typeLink -> typeLink.getText()).collect(Collectors.toSet());
		return allTypes.stream().allMatch(type -> containedTypes.contains(type));
	}

	public String getNodeKey() {
		Deque<String> ancestorKeys = new LinkedList<>();
		
		for (FhirTreeTableContent ancestor = this; ancestor != null; ancestor = ancestor.getParent()) {
			ancestorKeys.addFirst(ancestor.getKeySegment());
		}
		
		String key = String.join(".", ancestorKeys);
		return key;
	}

	public String getKeySegment() {
		String nodeKey = getPathName();
		
		// Sliced nodes need disambiguating for the details page otherwise we cannot link each element to its
		// unique details entry.
		if (discriminatorValue.isPresent()) {
			// Name is generally more readable and shorter than resolved slicing info, so prioritise that.
			// Slicing discriminator information should always be available as a backup.
			String alias;
			if (getName().isPresent()) {
				alias = name.get();
			} else {
				alias = discriminatorValue.get();
			}
			
			nodeKey += "(" + alias + ")";
		}
		
		return nodeKey;
	}
	
	public boolean hasSlicingSibling() {
		return parent != null
		  && parent.getChildren()
		  		.stream()
		  		.anyMatch(
		  			child -> child != this
		  			  && child.getPath().equals(getPath())
		  			  && child.hasSlicingInfo());
	}
	
	public FhirTreeTableContent getSlicingSibling() {
		List<? extends FhirTreeTableContent> slicingSiblings = parent.getChildren()
			.stream()
			.filter(child -> child.getPath().equals(getPath()) && child.hasSlicingInfo())
			.collect(Collectors.toList());
		
		if (slicingSiblings.size() > 1) {
			throw new IllegalStateException("More than 1 sibling with slicing present for " + getPath());
		}
		
		return slicingSiblings.get(0);
	}

	public boolean isExtension() {
		return extensionType.isPresent();
	}
	
	public boolean isSimpleExtension() {
		return extensionType.isPresent() &&
			extensionType.get().equals(ExtensionType.SIMPLE);
	}
	
	public boolean isComplexExtension() {
		return extensionType.isPresent() &&
			extensionType.get().equals(ExtensionType.COMPLEX);
	}

	public boolean useBackupTypeLinks() {
		return (typeLinks.isEmpty()
				&& backupNode != null
				&& !backupNode.getTypeLinks().isEmpty());
	}

	public String getInformation() {
		return information;
	}
	
	public List<ConstraintInfo> getConstraints() {
		return constraints;
	}

	public void addChild(int index, FhirTreeTableContent child) {
		children.add(index, child);
		child.setParent(this);
	}

	public void addChild(FhirTreeTableContent child) {
		children.add(child);
		child.setParent(this);
	}

	public FhirTreeTableContent getParent() {
		return parent;
	}

	public void setParent(FhirTreeTableContent fhirTreeNode) {
		this.parent = fhirTreeNode;
	}

	public List<? extends FhirTreeTableContent> getChildren() {
		return children;
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}

	private boolean isDirectlyRemovedByProfile() {
		return max.equals(Optional.of("0"));
	}
	
	public boolean isRemovedByProfile() {
		if (isDirectlyRemovedByProfile()) {
			return true;
		} else if (parent != null) {
			return parent.isRemovedByProfile();
		} else {
			return false;
		}
	}

	public String getPath() {
		return path;
	}

	public String getPathName() {
		String[] pathTokens = path.split("\\.");
		return pathTokens[pathTokens.length - 1];
	}

	public boolean isFixedValue() {
		return fixedValue.isPresent();
	}

	public Optional<String> getFixedValue() {
		return fixedValue;
	}

	public void setFixedValue(String fixedValue) {
		this.fixedValue = Optional.of(fixedValue);
	}
	
	public boolean hasExample() {
		return example.isPresent();
	}

	public Optional<String> getExample() {
		return example;
	}

	public void setExample(String exampleValue) {
		this.example = Optional.of(exampleValue);
	}

	public boolean hasDefaultValue() {
		return defaultValue.isPresent();
	}

	public Optional<String> getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = Optional.of(defaultValue);
	}

	public boolean hasBinding() {
		return binding.isPresent();
	}

	public Optional<BindingInfo> getBinding() {
		return binding;
	}

	public void setBinding(BindingInfo binding) {
		this.binding = Optional.of(binding);
	}

	public void setBackupNode(FhirTreeNode backupNode) {
		Preconditions.checkNotNull(backupNode);
		this.backupNode = backupNode;
	}

	@Override
	public boolean hasBackupNode() {
		return backupNode != null;
	}

	@Override
	public Optional<FhirTreeNode> getBackupNode() {
		return Optional.of(backupNode);
	}

	@Override
	public String toString() {
		return getPath();
	}

	public Optional<String> getRequirements() {
		return requirements;
	}

	public void setRequirements(String requirements) {
		this.requirements = Optional.of(requirements);
	}

	public Optional<String> getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = Optional.of(comments);
	}

	public List<String> getAliases() {
		return aliases;
	}

	public void setAliases(List<String> aliases) {
		this.aliases = aliases;
	}
	
	public Optional<String> getLinkedNodeName() {
		return linkedNodeName;
	}
	
	public void setLinkedNodeName(String nameLink) {
		this.linkedNodeName = Optional.of(nameLink);
	}
	
	public void setExtensionType(ExtensionType extensionType) {
		this.extensionType = Optional.of(extensionType);
	}
	
	public Optional<ExtensionType> getExtensionType() {
		return extensionType;
	}
	
	public void setLinkedNode(FhirTreeTableContent linkedNode) {
		this.linkedNode = Optional.of(linkedNode);
	}
	
	public Optional<FhirTreeTableContent> getLinkedNode() {
		if (linkedNodeName.isPresent()
		  && !linkedNode.isPresent()) {
			throw new IllegalStateException("Requesting linked node before it has been resolved");
		}
		
		if (!linkedNodeName.isPresent()
		  && linkedNode.isPresent()) {
			throw new IllegalStateException("Found linked node but wasn't expecting one");
		}
		
		return linkedNode;
	}
	
	private void setDiscriminatorValue(String discriminatorValue) {
		this.discriminatorValue = Optional.of(discriminatorValue);
	}

	public Set<FhirURL> getExtensionUrlDiscriminators() {
		return getTypeLinks()
			.stream()
			.filter(typeLink -> typeLink.getPrimaryLinkData().getText().equals("Extension"))
			.flatMap(typeLink -> typeLink instanceof NestedLinkData ? ((NestedLinkData)typeLink).getNestedLinks().stream() : Lists.newArrayList(typeLink.getPrimaryLinkData()).stream())
			.map(link -> link.getURL())
			.collect(Collectors.toSet());
	}

	public Optional<FhirTreeTableContent> findUniqueDescendantMatchingPath(String relativePath) {
		String fullPath = getPath() + "." + relativePath;
		
		List<FhirTreeTableContent> childNodesMatchingDiscriminatorPath = Lists.newArrayList();
		for (FhirTreeTableContent descendantNode : new FhirTreeData(this)) {
			if (descendantNode.getPath().equals(fullPath)) {
				if (descendantNode instanceof FhirTreeNode) {
					FhirTreeNode matchedFhirTreeNode = (FhirTreeNode)descendantNode;
					childNodesMatchingDiscriminatorPath.add(matchedFhirTreeNode);
				} else {
					throw new IllegalStateException("Snapshot tree contains a Dummy node");
				}
			}
		}
		
		FhirTreeTableContent discriminatorDescendant;
		if (childNodesMatchingDiscriminatorPath.size() == 1) {
			discriminatorDescendant = childNodesMatchingDiscriminatorPath.get(0);
		} else if (childNodesMatchingDiscriminatorPath.size() == 0) {
			discriminatorDescendant = null;
		} else {
			throw new IllegalStateException("Multiple descendants matching discriminator " + fullPath 
					+ " found for element at " + getPath());
		}
		
		return Optional.ofNullable(discriminatorDescendant);
	}

	public void cacheSlicingDiscriminator() {
		if (hasSlicingSibling()) {
			Set<String> discriminatorPaths = getSlicingSibling().getSlicingInfo().get().getDiscriminatorPaths();
			List<String> discriminators = Lists.newArrayList();
			
			if (discriminatorPaths.size() > 1) {
				throw new IllegalStateException("Don't yet handle multiple discriminators. Return a map? Consider ordering for node key?");
			}
			
			for (String discriminatorPath : discriminatorPaths) {
				if (getPathName().equals("extension") 
				  && discriminatorPath.equals("url")) {
					// special case
					
					Set<FhirURL> extensionUrlDiscriminators = getExtensionUrlDiscriminators();
					
					if (extensionUrlDiscriminators.size() == 0) {
						RendererError.handle(RendererError.Key.UNRESOLVED_DISCRIMINATOR, "Missing extension URL discriminator node (" + getPath() + "). "
							+ "If slicing node is removed, we may not know to include a disambiguator in fhirTreeNode.getKeySegment()");
						discriminators.add("<missing>");
					} else if (extensionUrlDiscriminators.size() > 1) {
						throw new IllegalStateException("Don't yet handle multiple extension url discriminators. Consider ordering so that keys are consistent?");
					} else {
						discriminators.add(extensionUrlDiscriminators.stream().findFirst().get().toFullString());
					}
				} else if (discriminatorPath.endsWith("@type")) {
					Optional<FhirTreeTableContent> discriminatorNode;
					if (discriminatorPath.equals("@type")) {
						discriminatorNode = Optional.of(this);
					} else {
						String relativePath = discriminatorPath.substring(0, discriminatorPath.length() - 1 - "@type".length());
						discriminatorNode = findUniqueDescendantMatchingPath(relativePath);
					}
					
					if (discriminatorNode.isPresent()) {
						// if the element is a reference type, we need to look at the type it is a reference to. Otherwise it's just the type string.
						List<LinkData> discriminatorNodeTypeLinks = discriminatorNode.get().getTypeLinks();
						for (LinkData discriminatorNodeTypeLink : discriminatorNodeTypeLinks) {
							if (discriminatorNodeTypeLink instanceof SimpleLinkData) {
								discriminators.add(((SimpleLinkData) discriminatorNodeTypeLink).getText());
							} else {
								throw new IllegalStateException("Don't yet handle @type discriminator node with nested type links (" + getPath() + " -> " + discriminatorPath + ")");
							}
						}
					} else {
						throw new IllegalStateException("Couldn't resolve discriminatorPath '" + discriminatorPath + "' for node " + getPath());
					}
				} else {
					// most discriminators
					
					Optional<FhirTreeTableContent> discriminatorNode = findUniqueDescendantMatchingPath(discriminatorPath);
					boolean foundNode = discriminatorNode.isPresent(); 
					boolean isFixed = foundNode && discriminatorNode.get().isFixedValue();
					boolean hasBinding = foundNode && discriminatorNode.get().hasBinding();
					
					if (!foundNode) {
						throw new IllegalStateException("Couldn't resolve discriminatorPath '" + discriminatorPath + "' for node " + getPath());
					// fixed value is simpler, so give it priority if we have both
					} else if (isFixed) {
						Optional<String> discriminatorValue = discriminatorNode.get().getFixedValue();
						discriminators.add(discriminatorValue.get());
					} else if (hasBinding) {
						BindingInfo bindingInfo = discriminatorNode.get().getBinding().get();
						if (bindingInfo.getUrl().isPresent()) {
							discriminators.add(bindingInfo.getDescription().get());
						} else {
							discriminators.add(bindingInfo.getUrl().get().toFullString());
						}
					} else {
						RendererError.handle(RendererError.Key.UNRESOLVED_DISCRIMINATOR, 
							"Expected Fixed Value or Binding on discriminator node at " + discriminatorPath + " for sliced node " + getPath());
						discriminators.add("<missing>");
					}
				}
			}
			
			if (discriminators.size() == 0) {
				RendererError.handle(RendererError.Key.NO_DISCRIMINATORS_FOUND, "Didn't find any discriminators to identify " + getPath() + " (likely caused by previous error)");
				setDiscriminatorValue("<missing>");
			} else if (discriminators.size() > 1) {
				throw new IllegalStateException("Found multiple discriminators: [" + String.join(", ", discriminators) + " ] for node " + getPath());
			} else {
				setDiscriminatorValue(discriminators.get(0));
			}
		}
	}

	@Override
	public boolean isPrimitive() {
		return getDataType().equals(FhirDataType.PRIMITIVE);
	}
	
	@Override
	public FhirDataType getDataType() {
		if (dataType.equals(FhirDataType.DELEGATED_TYPE)
		  && hasBackupNode()) {
			return backupNode.getDataType();
		} else {
			return dataType;
		}
	}

	public void addMapping(FhirElementMapping fhirElementMapping) {
		mappings.add(fhirElementMapping);
	}
	
	public List<FhirElementMapping> getMappings() {
		return mappings;
	}
}
