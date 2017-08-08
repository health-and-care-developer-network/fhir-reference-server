package uk.nhs.fhir.makehtml.data.structdef.tree;

import java.util.List;
import java.util.Optional;

import uk.nhs.fhir.makehtml.data.BindingInfo;
import uk.nhs.fhir.makehtml.data.FhirDataType;
import uk.nhs.fhir.makehtml.data.structdef.ConstraintInfo;
import uk.nhs.fhir.makehtml.data.structdef.ExtensionType;
import uk.nhs.fhir.makehtml.data.structdef.FhirCardinality;
import uk.nhs.fhir.makehtml.data.structdef.ResourceFlags;
import uk.nhs.fhir.makehtml.data.structdef.SlicingInfo;
import uk.nhs.fhir.makehtml.data.url.LinkDatas;

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
	LinkDatas getTypeLinks();

	String getDisplayName();

	ResourceFlags getResourceFlags();

	public boolean useBackupCardinality();
	FhirCardinality getCardinality();

	String getInformation();

	boolean hasSlicingInfo();
	Optional<SlicingInfo> getSlicingInfo();
	
	boolean isFixedValue();
	Optional<String> getFixedValue();

	List<String> getExamples();

	boolean hasDefaultValue();
	Optional<String> getDefaultValue();

	boolean hasBinding();
	Optional<BindingInfo> getBinding();

	void setBackupNode(FhirTreeNode backupNode);
	boolean hasBackupNode();
	Optional<FhirTreeNode> getBackupNode();
	
	public List<ConstraintInfo> getConstraints();
	
	public String getNodeKey();

	Optional<String> getName();

	public Optional<String> getDefinition();
	
	public Optional<String> getLinkedNodeName();
	public Optional<String> getLinkedNodeId();
	
	public Optional<ExtensionType> getExtensionType();

	boolean hasSlicingSibling();

	FhirTreeTableContent getSlicingSibling();

	String getKeySegment();

	boolean isPrimitive();

	FhirDataType getDataType();

	public Optional<String> getId();

	Optional<FhirTreeTableContent> getLinkedNode();
}
