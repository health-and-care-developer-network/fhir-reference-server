package uk.nhs.fhir.makehtml.data;

import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Optional;

public class DummyFhirTreeNode implements FhirTreeTableContent {

	private FhirTreeNode backup = null;
	private FhirTreeTableContent parent;
	private final List<FhirTreeTableContent> children = Lists.newArrayList();
	private final String path;
	
	public DummyFhirTreeNode(FhirTreeTableContent parent, String path) {
		//Preconditions.checkNotNull(parent);
		Preconditions.checkNotNull(path);
		
		this.parent = parent;
		this.path = path;
	}
	
	@Override
	public void addChild(FhirTreeTableContent child) {
		children.add(child);
		child.setParent(this);
	}

	@Override
	public FhirTreeTableContent getParent() {
		return parent;
	}
	
	@Override
	public String getPath() {
		return path;
	}

	@Override
	public List<FhirTreeTableContent> getChildren() {
		return children;
	}

	@Override
	public boolean hasChildren() {
		// a dummy node only exists to hold a non-dummy child
		if (children.isEmpty()) {
			throw new IllegalStateException("Dummy FHIR tree node without children");
		}
		
		return true;
	}

	@Override
	public void setParent(FhirTreeTableContent parent) {
		this.parent = parent;
	}

	@Override
	public boolean isRemovedByProfile() {
		return backup.isRemovedByProfile();
	}

	@Override
	public String getPathName() {
		String[] pathTokens = path.split("\\.");
		return pathTokens[pathTokens.length - 1];
	}

	@Override
	public boolean hasSlicingInfo() {
		return false;
	}
	@Override
	public Optional<SlicingInfo> getSlicingInfo() {
		throw new IllegalStateException("Dummy node cannot have slicing info");
	}

	@Override
	public List<LinkData> getTypeLinks() {
		return Lists.newArrayList();
	}

	@Override
	public boolean useBackupTypeLinks() {
		return true;
	}
	
	public List<ConstraintInfo> getConstraints() {
		return Lists.newArrayList();
	}

	@Override
	public void setFhirIcon(FhirIcon icon) {
		throw new IllegalStateException("Setting icon on a dummy node");
	}
	
	@Override
	public FhirIcon getFhirIcon() {
		return backup.getFhirIcon();
	}

	@Override
	public String getDisplayName() {
		if (backup != null) {
			return backup.getDisplayName();
		} else {
			return path;
		}
	}

	@Override
	public ResourceFlags getResourceFlags() {
		return new ResourceFlags();
	}

	@Override
	public FhirCardinality getCardinality() {
		return backup.getCardinality();
	}

	@Override
	public boolean useBackupCardinality() {
		return true;
	}

	@Override
	public String getInformation() {
		return "";
	}

	@Override
	public boolean isFixedValue() {
		return false;
	}

	@Override
	public Optional<String> getFixedValue() {
		throw new IllegalStateException("Dummy node cannot have fixed value info");
	}

	@Override
	public boolean hasExample() {
		return false;
	}

	@Override
	public Optional<String> getExample() {
		throw new IllegalStateException("Dummy node cannot have fixed example info");
	}

	@Override
	public boolean hasDefaultValue() {
		return false;
	}

	@Override
	public Optional<String> getDefaultValue() {
		throw new IllegalStateException("Dummy node cannot have fixed default value");
	}

	@Override
	public boolean hasBinding() {
		return false;
	}

	// KGM 9/May/2017
	@Override
	public boolean hasElement() { return false; }

	@Override
	public Optional<ElementDefinitionDt> getElement() { return Optional.empty(); }

	@Override
	public Optional<BindingInfo> getBinding() {
		throw new IllegalStateException("Dummy node cannot have fixed binding info");
	}
	
	public void setBackupNode(FhirTreeNode backupNode) {
		Preconditions.checkNotNull(backupNode);
		this.backup = backupNode;
	}

	@Override
	public boolean hasBackupNode() {
		return backup != null;
	}

	@Override
	public Optional<FhirTreeNode> getBackupNode() {
		return Optional.of(backup);
	}
	
	@Override
	public String toString() {
		return "{" + getPath() + "}";
	}
}
