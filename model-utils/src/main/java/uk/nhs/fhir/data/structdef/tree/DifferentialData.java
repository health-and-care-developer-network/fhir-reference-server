package uk.nhs.fhir.data.structdef.tree;

import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;

import uk.nhs.fhir.data.structdef.BindingInfo;
import uk.nhs.fhir.data.structdef.ConstraintInfo;
import uk.nhs.fhir.data.structdef.FhirElementDataType;
import uk.nhs.fhir.data.structdef.ResourceFlags;
import uk.nhs.fhir.data.structdef.SlicingInfo;
import uk.nhs.fhir.data.structdef.tree.tidy.HasBackupNode;
import uk.nhs.fhir.data.structdef.tree.tidy.HasSlicingInfo;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.StringUtil;

public class DifferentialData extends AbstractFhirTreeNodeData implements HasBackupNode<SnapshotData, SnapshotTreeNode>, HasSlicingInfo {

	protected final Optional<Integer> min;
	protected final Optional<String> max;

	private final SnapshotTreeNode backupNode;
	
	public DifferentialData(
			Optional<String> id,
			Optional<String> name,
			ResourceFlags flags,
			Optional<Integer> min,
			Optional<String> max,
			LinkDatas typeLinks,
			String information,
			List<ConstraintInfo> constraints,
			ImmutableNodePath path,
			FhirElementDataType dataType,
			FhirVersion version,
			SnapshotTreeNode backupNode) {
		super(id, name, flags, typeLinks, information, constraints, path, dataType, version);
		this.min = Preconditions.checkNotNull(min);
		this.max = Preconditions.checkNotNull(max);
		this.backupNode = Preconditions.checkNotNull(backupNode);
		
		if (!id.equals(backupNode.getData().getId())) {
			if (!id.isPresent()) {
				// Forge workaround - not a renderer bug
				EventHandlerContext.forThread().event(RendererEventType.DIFFERENTIAL_NODE_MISSING_ID, "No id found for differential, but found " + backupNode.getData().getId().get() + " on backup node");
			} else if (backupNode.getData().getId().get().contains("[x]")) {
				EventHandlerContext.forThread().event(RendererEventType.DIFFERENTIAL_CHOICE_NODE_WRONG_ID,
					"Id with choice on backup node (" + backupNode.getData().getId() + ") didn't match Id on differential node (" + id.get() + ")");
			} else {
				throw new IllegalStateException("id (" + id + ") doesn't match backup node id (" + backupNode.getData().getId() + ")");
			}
		}
	}
	
	@Override
	public SnapshotTreeNode getBackupNode() {
		return backupNode;
	}
	
	public SnapshotTreeNode getBackupNode(FhirTreeKeyLookup<SnapshotData, SnapshotTreeNode> lookup) {
		return lookup.nodeForKey(backupNode.getNodeKey());
	}
	
	public Optional<String> getDefinition() {
		return StringUtil.firstPresent(
			() -> definition, 
			() -> backupNode.getData().getDefinition());
	}

	@Override
	public Optional<BindingInfo> getBinding() {

		if (binding.isPresent()
		  && backupNode.getData().getBinding().isPresent()) {
			//combine with backup data
			return Optional.of(
				BindingInfo.resolveWithBackupData(
					binding.get(), 
					backupNode.getData().getBinding().get()));
			
		} else if (binding.isPresent()){
			return binding;
			
		} else {
			return backupNode.getData().getBinding();
		}
	}
	
	@Override
	public boolean useBackupCardinality() {
		return !min.isPresent() && !max.isPresent();
	}
	
	@Override
	public boolean useBackupTypeLinks() {
		return (typeLinks.isEmpty()
		  && !backupNode.getData().getTypeLinks().isEmpty());
	}

	@Override
	public LinkDatas getTypeLinks() {
		if (useBackupTypeLinks()) {
			return backupNode.getData().getTypeLinks();
		} else {
			return super.getTypeLinks();
		}
	}

	@Override
	public FhirElementDataType getDataType() {
		if (dataType.equals(FhirElementDataType.DELEGATED_TYPE)) {
			return backupNode.getData().getDataType();
		} else {
			return dataType;
		}
	}
	
	public Integer getMin() {
		return min.orElse(backupNode.getData().getMin());
	}
	
	public String getMax() {
		return max.orElse(backupNode.getData().getMax());
	}

	@Override
	public boolean hasSlicingInfo() {
		return super.hasSlicingInfo() || getBackupNode().getData().hasSlicingInfo();
	}
	
	@Override
	public Optional<SlicingInfo> getSlicingInfo() {
		return StringUtil.firstPresent(
			super.getSlicingInfo(), 
			getBackupNode().getData().getSlicingInfo());
	}
}
