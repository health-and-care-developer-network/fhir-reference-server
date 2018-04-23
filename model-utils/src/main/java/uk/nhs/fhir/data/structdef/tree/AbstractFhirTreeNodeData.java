package uk.nhs.fhir.data.structdef.tree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.structdef.BindingInfo;
import uk.nhs.fhir.data.structdef.ConstraintInfo;
import uk.nhs.fhir.data.structdef.Example;
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
import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;
import uk.nhs.fhir.util.FhirSimpleTypes;
import uk.nhs.fhir.util.FhirURLConstants;
import uk.nhs.fhir.util.FhirVersion;

public abstract class AbstractFhirTreeNodeData implements AbstractFhirTreeTableContent {

	protected final Optional<String> id;
	protected final Optional<String> name;
	protected final ResourceFlags resourceFlags;
	protected final LinkDatas typeLinks;
	protected final String information;
	protected final List<ConstraintInfo> constraints;
	protected final ImmutableNodePath path;
	protected final FhirElementDataType dataType;
	protected final FhirVersion version;
	
	public AbstractFhirTreeNodeData(
			Optional<String> id,
			Optional<String> name,
			ResourceFlags flags,
			LinkDatas typeLinks,
			String information,
			List<ConstraintInfo> constraints,
			ImmutableNodePath path,
			FhirElementDataType dataType,
			FhirVersion version) {
		this.id = id;
		this.name = name;
		this.resourceFlags = flags;
		this.typeLinks = typeLinks;
		this.information = information;
		this.constraints = constraints;
		this.path = path;
		this.dataType = dataType;
		this.version = version;
	}

	public Optional<String> getId() {
		return id;
	}

	public Optional<String> getName() {
		return name;
	}

	public ResourceFlags getResourceFlags() {
		return resourceFlags;
	}

	public String getDisplayName() {
		boolean hasSliceName = sliceName.isPresent() && !sliceName.get().isEmpty();
		String pathName = getPath().getPathName();
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

	private static final Map<String, String> fixableTypes = new HashMap<>();
	static {
		fixableTypes.put("extension", FhirURLConstants.HTTP_HL7_FHIR + "/extensibility.html#Extension");
	}
	
	public LinkDatas getTypeLinks() {
		if (typeLinks.isEmpty()
		  && (linkedNodeName.isPresent() || linkedNodeId.isPresent())) {
			String linkedContentKey = getLinkedNode().get().getNodeKey();
			typeLinks.addSimpleLink(
				new LinkData(FhirURL.buildOrThrow("details.html#" + linkedContentKey, version), "see " + linkedContentKey));
		}
		
		if (typeLinks.isEmpty()
		  && fixableTypes.containsKey(getPathName())) {
			EventHandlerContext.forThread().event(RendererEventType.FIX_MISSING_TYPE_LINK, "Filling in type link for " + getPath());
			typeLinks.addSimpleLink(new LinkData(FullFhirURL.buildOrThrow(fixableTypes.get(getPathName()), version), "Extension"));
		}

		if (typeLinks.isEmpty()
		  && !isRoot()) {
			EventHandlerContext.forThread().event(RendererEventType.MISSING_TYPE_LINK, "Couldn't find any typelinks for " + path);
		}
		
		if (getPathName().endsWith("[x]")
		  && hasAllTypes()) {
			// tidy up choice node
			FhirURL openTypeUrl = FhirURL.buildOrThrow(FhirURLConstants.versionBase(version) + "/datatypes.html#open", version);
			return new LinkDatas(new LinkData(openTypeUrl, "*"));
		} else {
			return typeLinks;
		}
	}
	
	private boolean hasAllTypes() {
		Set<String> containedTypes = typeLinks.links().stream().map(typeLink -> typeLink.getKey().getText()).collect(Collectors.toSet());
		return FhirSimpleTypes.CHOICE_SUFFIXES.stream().allMatch(type -> containedTypes.contains(type));
	}

	@Override
	public String getInformation() {
		return information;
	}

	@Override
	public List<ConstraintInfo> getConstraints() {
		return constraints;
	}

	@Override
	public boolean isPrimitive() {
		return getDataType().equals(FhirElementDataType.PRIMITIVE);
	}

	public FhirVersion getVersion() {
		return version;
	}
	
	protected Optional<SnapshotTreeNode> linkedNode = Optional.empty();
	
	protected Optional<ExtensionType> extensionType = Optional.empty();
	protected Optional<SlicingInfo> slicingInfo = Optional.empty();
	protected Optional<String> sliceName = Optional.empty();
	protected Optional<String> fixedValue = Optional.empty();
	protected List<Example> examples = Lists.newArrayList();
	protected Optional<String> defaultValue = Optional.empty();
	protected Optional<BindingInfo> binding = Optional.empty();
	protected Optional<String> definition = Optional.empty();
	protected Optional<String> requirements = Optional.empty();
	protected Optional<String> comments = Optional.empty();
	protected List<String> aliases = Lists.newArrayList();
	protected Optional<String> linkedNodeName = Optional.empty();
	protected Optional<String> linkedNodeId = Optional.empty();
	protected List<FhirElementMapping> mappings = Lists.newArrayList();

	public Optional<ExtensionType> getExtensionType() {
		return extensionType;
	}

	public void setExtensionType(Optional<ExtensionType> extensionType) {
		this.extensionType = extensionType;
	}

	public boolean isExtension() {
		return extensionType.isPresent();
	}

	public boolean isSimpleExtension() {
		return extensionType.isPresent()
			&& extensionType.get().equals(ExtensionType.SIMPLE);
	}

	public boolean isComplexExtension() {
		return extensionType.isPresent()
			&& extensionType.get().equals(ExtensionType.COMPLEX);
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

	public boolean isFixedValue() {
		return fixedValue.isPresent();
	}

	public Optional<String> getFixedValue() {
		return fixedValue;
	}

	public void setFixedValue(Optional<String> fixedValue) {
		this.fixedValue = fixedValue;
	}

	public List<Example> getExamples() {
		return examples;
	}

	public void setExamples(List<Example> examples) {
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

	public void setDefinition(Optional<String> definition) {
		this.definition = definition;
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

	public void setLinkedNode(SnapshotTreeNode linkedNode) {
		this.linkedNode = Optional.of(linkedNode);
	}
	
	public Optional<SnapshotTreeNode> getLinkedNode() {
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

	public void setMappings(List<FhirElementMapping> mappings) {
		this.mappings = mappings;
	}

	public List<FhirElementMapping> getMappings() {
		return mappings;
	}
	
	public void setDiscriminatorValue(String discriminatorValue) {
		this.discriminatorValue = Optional.of(discriminatorValue);
	}

	/*
	 * These fields cannot be set up-front since they require reasoning about the tree
	 * in which the node sits
	 */
	
	// An optimisation to prevent having to resolve a slicing discriminator many times
	protected Optional<String> discriminatorValue = Optional.empty();
	
	@Override
	public Optional<String> getDiscriminatorValue() {
		return discriminatorValue;
	}

	@Override
	public String toString() {
		return getPathString();
	}
	
	@Override
	public ImmutableNodePath getPath() {
		return path;
	}
	
	@Override
	public boolean isRoot() {
		return path.isRoot();
	}
	
	@Override
	public String getPathName() {
		return path.getPathName();
	}
	
	@Override
	public String getPathString() {
		return path.toString();
	}
	
	@Override
	public FhirCardinality getCardinality() {
		return new FhirCardinality(getMin(), getMax());
	}
	
	public List<FhirURL> getExtensionUrls() {
		List<FhirURL> urls = Lists.newArrayList();
		
		if (getPathName().equals("extension")) {
			LinkDatas typeLinks = getTypeLinks();
			for (Entry<LinkData, List<LinkData>> link : typeLinks.links()) {
				if (!link.getValue().isEmpty()
				  && link.getKey().getText().equals("Extension")) {
					for (LinkData nestedLink : link.getValue()) {
						urls.add(nestedLink.getURL());
					}
				}
			}
		}
		
		return urls;
	}
}
