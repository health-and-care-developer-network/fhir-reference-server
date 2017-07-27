package uk.nhs.fhir.makehtml.data;

import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class DummyFhirTreeNode implements FhirTreeTableContent {

	public static final boolean DISPLAY_DUMMY_NODE_TYPES = true;
	
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
		return backup.hasSlicingInfo();
	}
	@Override
	public Optional<SlicingInfo> getSlicingInfo() {
		return backup.getSlicingInfo();
	}

	@Override
	public LinkDatas getTypeLinks() {
		if (DISPLAY_DUMMY_NODE_TYPES) {
			return backup.getTypeLinks();
		} else {
			return new LinkDatas();
		}
	}

	@Override
	public boolean useBackupTypeLinks() {
		return true;
	}
	
	public List<ConstraintInfo> getConstraints() {
		return Lists.newArrayList();
	}

	@Override
	public void setFhirIcon(FhirDstu2Icon icon) {
		throw new IllegalStateException("Setting icon on a dummy node");
	}
	
	@Override
	public Optional<FhirDstu2Icon> getFhirIcon() {
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
	public List<String> getExamples() {
		return Lists.newArrayList();
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

	@Override
	public String getNodeKey() {
		return backup.getNodeKey();
	}

	@Override
	public Optional<String> getName() {
		return backup.getName();
	}

	@Override
	public Optional<String> getDefinition() {
		return backup.getDefinition();
	}

	@Override
	public Optional<String> getLinkedNodeName() {
		return backup.getLinkedNodeName();
	}
	
	@Override
	public Optional<ExtensionType> getExtensionType() {
		return backup.getExtensionType();
	}

	@Override
	public boolean hasSlicingSibling() {
		return backup.hasSlicingSibling();
	}

	@Override
	public FhirTreeTableContent getSlicingSibling() {
		return backup.getSlicingSibling();
	}

	@Override
	public String getKeySegment() {
		return backup.getKeySegment();
	}

	@Override
	public boolean isPrimitive() {
		return backup.isPrimitive();
	}

	@Override
	public FhirDataType getDataType() {
		return backup.getDataType();
	}

	@Override
	public Optional<String> getId() {
		return backup.getId();
	}

	@Override
	public Optional<String> getLinkedNodeId() {
		return backup.getLinkedNodeId();
	}
}
