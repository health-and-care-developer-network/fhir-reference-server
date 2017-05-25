package uk.nhs.fhir.makehtml.data;

import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import uk.nhs.fhir.makehtml.NewMain;

public class FhirTreeNode implements FhirTreeTableContent {
	private FhirIcon icon;
	private final Optional<String> name;
	private final ResourceFlags resourceFlags;
	private final Optional<Integer> min;
	private final Optional<String> max;
	//private final FhirCardinality cardinality;
	private final List<LinkData> typeLinks;
	private final String information;
	private final List<ConstraintInfo> constraints;
	private final String path;

	private Optional<SlicingInfo> slicingInfo = Optional.empty();
	private Optional<String> fixedValue = Optional.empty();
	private Optional<String> fixedUri = Optional.empty();
	private Optional<String> example = Optional.empty();
	private Optional<String> defaultValue = Optional.empty();
	private Optional<BindingInfo> binding = Optional.empty();
	private Optional<ElementDefinitionDt> element = Optional.empty();
	private Optional<String> definition = Optional.empty();
	private Optional<String> requirements = Optional.empty();
	private Optional<String> comments = Optional.empty();
	private List<String> aliases = Lists.newArrayList();

	private FhirTreeTableContent parent = null;
	private FhirTreeNode backupNode = null;

	private final List<FhirTreeTableContent> children = Lists.newArrayList();

	public FhirTreeNode(
			FhirIcon icon,
			Optional<String> name,
			ResourceFlags flags,
			Integer min,
			String max,
			//FhirCardinality cardinality,
			List<LinkData> typeLinks,
			String information,
			List<ConstraintInfo> constraints,
			String path) {
		this.icon = icon;
		this.name = name;
		this.resourceFlags = flags;
		this.min = Optional.ofNullable(min);
		this.max = Optional.ofNullable(max);
		//this.cardinality = cardinality;
		this.typeLinks = typeLinks;
		this.information = information;
		this.constraints = constraints;
		this.path = path;
	}

	public FhirTreeNode(
			FhirIcon icon,
			Optional<String> name,
			ResourceFlags flags,
			Integer min,
			String max,
			//FhirCardinality cardinality,
			List<LinkData> typeLinks,
			String information,
			List<ConstraintInfo> constraints,
			String path,
			ElementDefinitionDt element ) {
		this.icon = icon;
		this.name = name;
		this.resourceFlags = flags;
		this.min = Optional.ofNullable(min);
		this.max = Optional.ofNullable(max);
		//this.cardinality = cardinality;
		this.typeLinks = typeLinks;
		this.information = information;
		this.constraints = constraints;
		this.path = path;
		this.element = Optional.ofNullable(element);
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
				if (backupNode == null 
				  && !NewMain.STRICT) {
					e.printStackTrace();
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

	public List<LinkData> getTypeLinks() {
		if (useBackupTypeLinks()) {
			return backupNode.getTypeLinks();
		}

		if (typeLinks.isEmpty()
		  && FhirTypeByPath.recognisedPath(getPath())) {

			LinkData linkForPath = FhirTypeByPath.forPath(getPath());
			typeLinks.add(linkForPath);
		}

		if (NewMain.STRICT
		  && typeLinks.isEmpty()) {
			throw new IllegalStateException("Couldn't find any typelinks for " + getPath());
		}

		return typeLinks;
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

	public boolean isRemovedByProfile() {
		return max.equals(Optional.of("0"));
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

	// KGM Added Element 9/May/2017
	public Optional<ElementDefinitionDt> getElement() { return this.element; }

	public boolean hasElement() { return this.element.isPresent(); }

	public void setElement(ElementDefinitionDt exampleValue) {
		this.element = Optional.ofNullable(exampleValue);
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
	
}
