package uk.nhs.fhir.data.structdef.tree;

import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;

import uk.nhs.fhir.data.structdef.ConstraintInfo;
import uk.nhs.fhir.data.structdef.FhirElementDataType;
import uk.nhs.fhir.data.structdef.ResourceFlags;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;
import uk.nhs.fhir.util.FhirVersion;

public class SnapshotData extends AbstractFhirTreeNodeData {

	protected final Integer min;
	protected final String max;
	
	public SnapshotData(Optional<String> id, Optional<String> name, ResourceFlags flags, Integer min, String max, LinkDatas typeLinks,
			String information, List<ConstraintInfo> constraints, ImmutableNodePath path, FhirElementDataType dataType,
			FhirVersion version) {
		super(id, name, flags, typeLinks, information, constraints, path, dataType, version);

		if (min == null) {
			EventHandlerContext.forThread().event(RendererEventType.MISSING_CARDINALITY, "min missing for node " + path);
			min = 0;
		}
		if (max == null) {
			EventHandlerContext.forThread().event(RendererEventType.MISSING_CARDINALITY, "max missing for node " + path);
			max = "*";
		}
		
		this.min = Preconditions.checkNotNull(min);
		this.max = Preconditions.checkNotNull(max);
	}

	public Optional<String> getDefinition() {
		return definition;
	}
	
	@Override
	public boolean useBackupCardinality() {
		return false;
	}
	
	@Override
	public Integer getMin() {
		return min;
	}
	
	@Override
	public String getMax() {
		return max;
	}

	@Override
	public FhirElementDataType getDataType() {
		return dataType;
	}

	public boolean useBackupTypeLinks() {
		return false;
	}
}
