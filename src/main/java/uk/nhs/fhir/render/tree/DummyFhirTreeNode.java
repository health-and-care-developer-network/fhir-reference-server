package uk.nhs.fhir.render.tree;

import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import uk.nhs.fhir.data.structdef.BindingInfo;
import uk.nhs.fhir.data.structdef.ConstraintInfo;
import uk.nhs.fhir.data.structdef.ExtensionType;
import uk.nhs.fhir.data.structdef.FhirCardinality;
import uk.nhs.fhir.data.structdef.FhirElementDataType;
import uk.nhs.fhir.data.structdef.ResourceFlags;
import uk.nhs.fhir.data.structdef.SlicingInfo;
import uk.nhs.fhir.data.url.LinkDatas;

public class DummyFhirTreeNode extends AbstractFhirTreeTableContent {

	public static final boolean DISPLAY_DUMMY_NODE_TYPES = true;
	
	private FhirTreeNode backup = null;
	private final String path;
	
	public DummyFhirTreeNode(AbstractFhirTreeTableContent parent, String path) {
		super(parent);
		
		this.path = Preconditions.checkNotNull(path);
	}
	
	@Override
	public String getPath() {
		return path;
	}

	@Override
	public boolean hasChildren() {
		// a dummy node only exists to hold a non-dummy child
		if (getChildren().isEmpty()) {
			throw new IllegalStateException("Dummy FHIR tree node without children");
		}
		
		return true;
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
		this.backup = Preconditions.checkNotNull(backupNode);
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
	public AbstractFhirTreeTableContent getSlicingSibling() {
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
	public FhirElementDataType getDataType() {
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

	@Override
	public Optional<AbstractFhirTreeTableContent> getLinkedNode() {
		return backup.getLinkedNode();
	}
	
	@Override
	public boolean isRoot() {
		return !path.contains(".");
	}
}
