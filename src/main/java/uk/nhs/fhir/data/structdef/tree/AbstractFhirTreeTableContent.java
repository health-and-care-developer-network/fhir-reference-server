package uk.nhs.fhir.data.structdef.tree;

import java.util.List;
import java.util.Optional;

import uk.nhs.fhir.data.structdef.BindingInfo;
import uk.nhs.fhir.data.structdef.ConstraintInfo;
import uk.nhs.fhir.data.structdef.ExtensionType;
import uk.nhs.fhir.data.structdef.FhirCardinality;
import uk.nhs.fhir.data.structdef.FhirElementDataType;
import uk.nhs.fhir.data.structdef.ResourceFlags;
import uk.nhs.fhir.data.structdef.tree.tidy.HasPath;
import uk.nhs.fhir.data.structdef.tree.tidy.HasSlicingInfo;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.util.FhirVersion;

public interface AbstractFhirTreeTableContent extends HasMappings, HasSlicingInfo, HasPath, MaybePrimitive, HasId {

	public boolean isRoot();

	public boolean useBackupTypeLinks();
	public LinkDatas getTypeLinks();

	public String getDisplayName();

	public ResourceFlags getResourceFlags();

	public Integer getMin();
	public String getMax();
	/**
	 * True if a node doesn't contain ANY cardinality information
	 */
	public boolean useBackupCardinality();
	public FhirCardinality getCardinality();

	public String getInformation();
	
	public boolean isFixedValue();
	public Optional<String> getFixedValue();

	public List<String> getExamples();

	public boolean hasDefaultValue();
	public Optional<String> getDefaultValue();

	public boolean hasBinding();
	public Optional<BindingInfo> getBinding();
	
	public List<ConstraintInfo> getConstraints();

	public abstract Optional<String> getName();

	public Optional<String> getDefinition();
	
	public Optional<SnapshotTreeNode> getLinkedNode();
	
	public Optional<String> getLinkedNodeName();
	public Optional<String> getLinkedNodeId();
	
	public Optional<ExtensionType> getExtensionType();

	public FhirElementDataType getDataType();
	
	public Optional<String> getDiscriminatorValue();
	
	public void setSliceName(Optional<String> sliceName);

	public boolean isExtension();
	public boolean isSimpleExtension();
	public boolean isComplexExtension();
	public void setExtensionType(Optional<ExtensionType> extensionType);
	
	public FhirVersion getVersion();
}
