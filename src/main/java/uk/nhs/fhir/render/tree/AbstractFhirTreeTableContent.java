package uk.nhs.fhir.render.tree;

import java.util.List;
import java.util.Optional;

import uk.nhs.fhir.data.structdef.BindingInfo;
import uk.nhs.fhir.data.structdef.ConstraintInfo;
import uk.nhs.fhir.data.structdef.ExtensionType;
import uk.nhs.fhir.data.structdef.FhirCardinality;
import uk.nhs.fhir.data.structdef.FhirElementDataType;
import uk.nhs.fhir.data.structdef.ResourceFlags;
import uk.nhs.fhir.data.structdef.SlicingInfo;
import uk.nhs.fhir.data.url.LinkDatas;

public abstract class AbstractFhirTreeTableContent extends TreeContent<AbstractFhirTreeTableContent> {
	public AbstractFhirTreeTableContent() {
	}
	
	public AbstractFhirTreeTableContent(AbstractFhirTreeTableContent parent) {
		super(parent);
	}

	public abstract boolean hasChildren();

	public abstract boolean isRemovedByProfile();

	public abstract String getPathName();

	public abstract boolean useBackupTypeLinks();
	public abstract LinkDatas getTypeLinks();

	public abstract String getDisplayName();

	public abstract ResourceFlags getResourceFlags();

	public abstract boolean useBackupCardinality();
	public abstract FhirCardinality getCardinality();

	public abstract String getInformation();

	public abstract boolean hasSlicingInfo();
	public abstract Optional<SlicingInfo> getSlicingInfo();
	
	public abstract boolean isFixedValue();
	public abstract Optional<String> getFixedValue();

	public abstract List<String> getExamples();

	public abstract boolean hasDefaultValue();
	public abstract Optional<String> getDefaultValue();

	public abstract boolean hasBinding();
	public abstract Optional<BindingInfo> getBinding();

	public abstract void setBackupNode(FhirTreeNode backupNode);
	public abstract boolean hasBackupNode();
	public abstract Optional<FhirTreeNode> getBackupNode();
	
	abstract public List<ConstraintInfo> getConstraints();
	
	abstract public String getNodeKey();

	abstract Optional<String> getName();

	abstract public Optional<String> getDefinition();
	
	abstract public Optional<String> getLinkedNodeName();
	abstract public Optional<String> getLinkedNodeId();
	
	abstract public Optional<ExtensionType> getExtensionType();

	public abstract boolean hasSlicingSibling();

	abstract AbstractFhirTreeTableContent getSlicingSibling();

	abstract String getKeySegment();

	public abstract boolean isPrimitive();

	public abstract FhirElementDataType getDataType();

	abstract public Optional<String> getId();

	public abstract Optional<AbstractFhirTreeTableContent> getLinkedNode();
	
	abstract public boolean isRoot();
}
