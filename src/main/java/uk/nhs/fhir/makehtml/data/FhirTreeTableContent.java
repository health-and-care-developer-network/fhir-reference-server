package uk.nhs.fhir.makehtml.data;

import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;

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

	FhirIcon getFhirIcon();

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

	// KGM Added Element 9/May/2017
	boolean hasElement();
	Optional<ElementDefinitionDt> getElement();

	void setBackupNode(FhirTreeNode backupNode);
	boolean hasBackupNode();
	Optional<FhirTreeNode> getBackupNode();

	void setFhirIcon(FhirIcon icon);
	
	public List<ConstraintInfo> getConstraints();
}
