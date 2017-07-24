package uk.nhs.fhir.makehtml.data;

import java.util.List;
import java.util.Optional;

public interface FhirTreeTableContent {

	void addChild(FhirTreeTableContent child);

	FhirTreeTableContent getParent();

	String getPath();

	List<? extends FhirTreeTableContent> getChildren();

	boolean hasChildren();

	void setParent(FhirTreeTableContent fhirTreeNode);

	boolean isRemovedByProfile();

	String getPathName();

	public boolean useBackupTypeLinks();
	List<LinkData> getTypeLinks();

	Optional<FhirDstu2Icon> getFhirIcon();

	String getDisplayName();

	ResourceFlags getResourceFlags();

	public boolean useBackupCardinality();
	FhirCardinality getCardinality();

	String getInformation();

	boolean hasSlicingInfo();
	Optional<SlicingInfo> getSlicingInfo();
	
	boolean isFixedValue();
	Optional<String> getFixedValue();

	boolean hasExample();
	Optional<String> getExample();

	boolean hasDefaultValue();
	Optional<String> getDefaultValue();

	boolean hasBinding();
	Optional<BindingInfo> getBinding();

	void setBackupNode(FhirTreeNode backupNode);
	boolean hasBackupNode();
	Optional<FhirTreeNode> getBackupNode();

	void setFhirIcon(FhirDstu2Icon icon);
	
	public List<ConstraintInfo> getConstraints();
	
	public String getNodeKey();

	Optional<String> getName();

	public Optional<String> getDefinition();
	
	public Optional<String> getLinkedNodeName();
	
	public Optional<ExtensionType> getExtensionType();

	boolean hasSlicingSibling();

	FhirTreeTableContent getSlicingSibling();

	String getKeySegment();

	boolean isPrimitive();

	FhirDataType getDataType();
}
