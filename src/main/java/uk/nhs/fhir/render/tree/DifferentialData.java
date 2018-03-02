package uk.nhs.fhir.render.tree;

import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;

import uk.nhs.fhir.data.structdef.BindingInfo;
import uk.nhs.fhir.data.structdef.ConstraintInfo;
import uk.nhs.fhir.data.structdef.FhirCardinality;
import uk.nhs.fhir.data.structdef.FhirElementDataType;
import uk.nhs.fhir.data.structdef.ResourceFlags;
import uk.nhs.fhir.data.structdef.SlicingInfo;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.render.tree.tidy.HasBackupNode;
import uk.nhs.fhir.render.tree.tidy.HasSlicingInfo;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.StringUtil;

public class DifferentialData extends AbstractFhirTreeNodeData implements HasBackupNode<SnapshotData, SnapshotTreeNode>, HasSlicingInfo {

	protected final Optional<Integer> min;
	protected final Optional<String> max;

	private final SnapshotTreeNode backupNode;
	
	public DifferentialData(
			Optional<String> name,
			ResourceFlags flags,
			Optional<Integer> min,
			Optional<String> max,
			LinkDatas typeLinks,
			String information,
			List<ConstraintInfo> constraints,
			String path,
			FhirElementDataType dataType,
			FhirVersion version,
			SnapshotTreeNode backupNode) {
		super(name, flags, typeLinks, information, constraints, path, dataType, version);
		this.min = Preconditions.checkNotNull(min);
		this.max = Preconditions.checkNotNull(max);
		this.backupNode = Preconditions.checkNotNull(backupNode);
	}
	
	@Override
	public SnapshotTreeNode getBackupNode() {
		return backupNode;
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

	public FhirCardinality getCardinality() {
		if (min.isPresent() && max.isPresent()) {
			return new FhirCardinality(min.get(), max.get());
		} else {
			/*try {
				Integer resolvedMin = min.orElse(backupNode.getData().getMin().get());
				String resolvedMax = max.orElse(backupNode.getData().getMax().get());
				return new FhirCardinality(resolvedMin, resolvedMax);
			} catch (NullPointerException | NoSuchElementException e) {
				if (!backupNode.getData().getMin().isPresent()
				  || !backupNode.getData().getMax().isPresent()) {
					EventHandlerContext.forThread().event(RendererEventType.MISSING_CARDINALITY, "Missing cardinality for " + getPath() + ": " + min + ".." + max, Optional.of(e));
					return new FhirCardinality(0, "*");
				} else {
					throw e;
				}
			}*/
			Integer resolvedMin = min.orElse(backupNode.getData().expectMin());
			String resolvedMax = max.orElse(backupNode.getData().expectMax());
			return new FhirCardinality(resolvedMin, resolvedMax);
		}
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
	
	public Optional<Integer> getMin() {
		return min;
	}
	
	public Optional<String> getMax() {
		return max;
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
