package uk.nhs.fhir.data.structdef.tree;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.nhs.fhir.FhirURLConstants;
import uk.nhs.fhir.data.structdef.BindingInfo;
import uk.nhs.fhir.data.structdef.ConstraintInfo;
import uk.nhs.fhir.data.structdef.ExtensionType;
import uk.nhs.fhir.data.structdef.FhirCardinality;
import uk.nhs.fhir.data.structdef.FhirElementDataType;
import uk.nhs.fhir.data.structdef.FhirElementMapping;
import uk.nhs.fhir.data.structdef.ResourceFlags;
import uk.nhs.fhir.data.structdef.SlicingInfo;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.FullFhirURL;
import uk.nhs.fhir.data.url.LinkData;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.makehtml.RendererError;
import uk.nhs.fhir.makehtml.RendererErrorConfig;
import uk.nhs.fhir.util.FhirVersion;

public class FhirTreeNode implements FhirTreeTableContent {
	private final Optional<String> name;
	private final ResourceFlags resourceFlags;
	private final Optional<Integer> min;
	private final Optional<String> max;
	private final LinkDatas typeLinks;
	private final String information;
	private final List<ConstraintInfo> constraints;
	private final String path;
	private final FhirElementDataType dataType;
	private final FhirVersion version;

	private Optional<String> id = Optional.empty();
	private Optional<ExtensionType> extensionType = Optional.empty();
	private Optional<SlicingInfo> slicingInfo = Optional.empty();
	private Optional<String> sliceName = Optional.empty();
	private Optional<String> fixedValue = Optional.empty();
	private List<String> examples = Lists.newArrayList();
	private Optional<String> defaultValue = Optional.empty();
	private Optional<BindingInfo> binding = Optional.empty();
	private Optional<String> definition = Optional.empty();
	private Optional<String> requirements = Optional.empty();
	private Optional<String> comments = Optional.empty();
	private List<String> aliases = Lists.newArrayList();
	private Optional<String> linkedNodeName = Optional.empty();
	private Optional<String> linkedNodeId = Optional.empty();
	private Optional<FhirTreeTableContent> linkedNode = Optional.empty();
	private Optional<String> discriminatorValue = Optional.empty();
	private List<FhirElementMapping> mappings = Lists.newArrayList();

	private FhirTreeTableContent parent = null;
	private FhirTreeNode backupNode = null;

	private final List<FhirTreeTableContent> children = Lists.newArrayList();

	public FhirTreeNode(
			Optional<String> name,
			ResourceFlags flags,
			Integer min,
			String max,
			LinkDatas typeLinks,
			String information,
			List<ConstraintInfo> constraints,
			String path,
			FhirElementDataType dataType,
			FhirVersion version) {
		this.name = name;
		this.resourceFlags = flags;
		this.min = Optional.ofNullable(min);
		this.max = Optional.ofNullable(max);
		this.typeLinks = typeLinks;
		this.information = information;
		this.constraints = constraints;
		this.path = path;
		this.dataType = dataType;
		this.version = version;
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

	public Optional<String> getName() {
		return name;
	}

	public String getDisplayName() {
		boolean hasSliceName = sliceName.isPresent() && !sliceName.get().isEmpty();
		String pathName = getPathName();
		boolean hasPath = !pathName.isEmpty();

		String displayName;
		if (hasSliceName && hasPath && !pathName.equals(sliceName.get())) {
			displayName = pathName + " (" + sliceName.get() + ")";
		} else if (hasPath) {
			displayName = pathName;
		} else if (hasSliceName) {
			displayName = sliceName.get();
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

	public void setSlicingInfo(Optional<SlicingInfo> slicingInfo) {
		this.slicingInfo = slicingInfo;
	}

	public void setSliceName(Optional<String> sliceName) {
		this.sliceName = sliceName;
	}

	public Optional<String> getSliceName() {
		return sliceName;
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
				Integer resolvedMin = min.orElse(backupNode.getMin().get());
				String resolvedMax = max.orElse(backupNode.getMax().get());
				return new FhirCardinality(resolvedMin, resolvedMax);
			} catch (NullPointerException | NoSuchElementException e) {
				if (backupNode == null
				  || !backupNode.getMin().isPresent()
				  || !backupNode.getMax().isPresent()) {
					RendererErrorConfig.handle(RendererError.MISSING_CARDINALITY, "Missing cardinality for " + getPath() + ": " + min + ".." + max, Optional.of(e));
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

	private static final Map<String, String> fixableTypes = new HashMap<>();
	static {
		fixableTypes.put("extension", FhirURLConstants.HTTP_HL7_FHIR + "/extensibility.html#Extension");
	}
	
	public LinkDatas getTypeLinks() {
		if (useBackupTypeLinks()) {
			return backupNode.getTypeLinks();
		}
		
		if (typeLinks.isEmpty()
		  && (linkedNodeName.isPresent() || linkedNodeId.isPresent())) {
			String linkedContentKey = getLinkedNode().get().getNodeKey();
			typeLinks.addSimpleLink(
				new LinkData(FhirURL.buildOrThrow("details.html#" + linkedContentKey, version), "see " + linkedContentKey));
		}
		
		if (typeLinks.isEmpty()
		  && fixableTypes.containsKey(getPathName())) {
			RendererErrorConfig.handle(RendererError.FIX_MISSING_TYPE_LINK, "Filling in type link for " + getPath());
			typeLinks.addSimpleLink(new LinkData(FullFhirURL.buildOrThrow(fixableTypes.get(getPathName()), version), "Extension"));
		} 

		if (typeLinks.isEmpty()
		  && !isRoot()) {
			RendererErrorConfig.handle(RendererError.MISSING_TYPE_LINK, "Couldn't find any typelinks for " + path);
		}
		
		if (getPathName().endsWith("[x]")
		  && hasAllTypes()) {
			FhirURL openTypeUrl = FhirURL.buildOrThrow(FhirURLConstants.versionBase(version) + "/datatypes.html#open", version);
			return new LinkDatas(new LinkData(openTypeUrl, "*"));
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
		
		Set<String> containedTypes = typeLinks.links().stream().map(typeLink -> typeLink.getKey().getText()).collect(Collectors.toSet());
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
			if (sliceName.isPresent()) {
				alias = sliceName.get();
			} else if (name.isPresent()) {
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

	public void setFixedValue(Optional<String> fixedValue) {
		this.fixedValue = fixedValue;
	}

	public List<String> getExamples() {
		return examples;
	}

	public void setExamples(List<String> examples) {
		this.examples = examples;
	}

	public boolean hasDefaultValue() {
		return defaultValue.isPresent();
	}

	public Optional<String> getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Optional<String> defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean hasBinding() {
		return binding.isPresent();
	}

	public Optional<BindingInfo> getBinding() {
		return binding;
	}

	public void setBinding(Optional<BindingInfo> binding) {
		this.binding = binding;
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
		return getNodeKey();
	}

	public Optional<String> getRequirements() {
		return requirements;
	}

	public void setRequirements(Optional<String> requirements) {
		this.requirements = requirements;
	}

	public Optional<String> getComments() {
		return comments;
	}

	public void setComments(Optional<String> comments) {
		this.comments = comments;
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
	
	public void setLinkedNodeName(Optional<String> nameLink) {
		this.linkedNodeName = nameLink;
	}
	
	public Optional<String> getLinkedNodeId() {
		return linkedNodeId;
	}

	public void setLinkedNodeId(Optional<String> linkedNodeId) {
		this.linkedNodeId = linkedNodeId;
	}
	
	public void setExtensionType(Optional<ExtensionType> extensionType) {
		this.extensionType = extensionType;
	}
	
	public Optional<ExtensionType> getExtensionType() {
		return extensionType;
	}
	
	public void setLinkedNode(FhirTreeTableContent linkedNode) {
		this.linkedNode = Optional.of(linkedNode);
	}
	
	public Optional<FhirTreeTableContent> getLinkedNode() {
		if ((linkedNodeName.isPresent() || linkedNodeId.isPresent())
		  && !linkedNode.isPresent()) {
			
			String linkDesc;
			if (linkedNodeName.isPresent()) {
				linkDesc = "[name: " + linkedNodeName.get() + "]";
			} else {
				linkDesc = "[id: " + linkedNodeId.get() + "]";
			}
			
			throw new IllegalStateException("Requesting linked node before it has been resolved " + linkDesc + " (" + getPath() + ")");
		}
		
		if (!(linkedNodeName.isPresent() || linkedNodeId.isPresent())
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
			.links()
			.stream()
			.filter(typeLink -> typeLink.getKey().getText().equals("Extension"))
			.flatMap(typeLink -> typeLink.getValue().isEmpty() ? Lists.newArrayList(typeLink.getKey()).stream() : typeLink.getValue().stream())
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
					// special case - check for an extension type link first. Otherwise we default to an actual child node 'url' as normal
					Set<FhirURL> extensionUrlDiscriminators =
						getTypeLinks()
							.links()
							.stream()
							.filter(typeLink -> typeLink.getKey().getText().equals("Extension"))
							.flatMap(typeLink -> typeLink.getValue().isEmpty() ? Lists.newArrayList(typeLink.getKey()).stream() : typeLink.getValue().stream())
							.map(link -> link.getURL())
							.collect(Collectors.toSet());
					
					if (extensionUrlDiscriminators.size() == 0) {
						RendererErrorConfig.handle(RendererError.UNRESOLVED_DISCRIMINATOR, "Missing extension URL discriminator node (" + getPath() + "). "
								+ "If slicing node is removed, we may not know to include a disambiguator in fhirTreeNode.getKeySegment()");
						discriminators.add("<missing>");
					} else if (extensionUrlDiscriminators.size() > 1) {
						throw new IllegalStateException("Don't yet handle multiple extension url discriminators. Consider ordering so that keys are consistent?");
					} else if (!extensionUrlDiscriminators.iterator().next().equals(FhirURL.buildOrThrow("http://hl7.org/fhir/stu3/extensibility.html#Extension", version))){
						discriminators.add(extensionUrlDiscriminators.iterator().next().toFullString());
					}
				}
				
				if (discriminators.isEmpty()) {
					if (discriminatorPath.endsWith("@type")) {
						Optional<FhirTreeTableContent> discriminatorNode;
						if (discriminatorPath.equals("@type")) {
							discriminatorNode = Optional.of(this);
						} else {
							String relativePath = discriminatorPath.substring(0, discriminatorPath.length() - 1 - "@type".length());
							discriminatorNode = findUniqueDescendantMatchingPath(relativePath);
						}
						
						if (discriminatorNode.isPresent()) {
							// if the element is a reference type, we need to look at the type it is a reference to. Otherwise it's just the type string.
							LinkDatas discriminatorNodeTypeLinks = discriminatorNode.get().getTypeLinks();
							for (Entry<LinkData, List<LinkData>> discriminatorNodeTypeLink : discriminatorNodeTypeLinks.links()) {
								if (discriminatorNodeTypeLink.getValue().isEmpty()) {
									discriminators.add(discriminatorNodeTypeLink.getKey().getText());
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
							if (!getSliceName().isPresent()) {
								RendererErrorConfig.handle(RendererError.UNRESOLVED_DISCRIMINATOR, 
									"Expected Fixed Value or Binding on discriminator node at " + discriminatorPath + " for sliced node " + getPath());
							}
							discriminators.add("<missing>");
						}
					}
				}
			}
			
			if (discriminators.size() == 0) {
				if (!getSliceName().isPresent()) {
					RendererErrorConfig.handle(RendererError.NO_DISCRIMINATORS_FOUND, "Didn't find any discriminators to identify " + getPath() + " (likely caused by previous error)");
				}
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
		return getDataType().equals(FhirElementDataType.PRIMITIVE);
	}
	
	@Override
	public FhirElementDataType getDataType() {
		if (dataType.equals(FhirElementDataType.DELEGATED_TYPE)
		  && hasBackupNode()) {
			return backupNode.getDataType();
		} else {
			return dataType;
		}
	}

	public void setMappings(List<FhirElementMapping> mappings) {
		this.mappings = mappings;
	}
	
	public List<FhirElementMapping> getMappings() {
		return mappings;
	}

	public Optional<String> getId() {
		return id;
	}
	public void setId(Optional<String> id) {
		this.id = id;
	}
	
	@Override
	public boolean isRoot() {
		return !path.contains(".");
	}
}
