package uk.nhs.fhir.render.tree;

import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;

import uk.nhs.fhir.data.structdef.ConstraintInfo;
import uk.nhs.fhir.data.structdef.FhirCardinality;
import uk.nhs.fhir.data.structdef.FhirElementDataType;
import uk.nhs.fhir.data.structdef.ResourceFlags;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.util.FhirVersion;

public class SnapshotData extends AbstractFhirTreeNodeData {

	protected final Integer min;
	protected final String max;
	
	public SnapshotData(Optional<String> name, ResourceFlags flags, Integer min, String max, LinkDatas typeLinks,
			String information, List<ConstraintInfo> constraints, String path, FhirElementDataType dataType,
			FhirVersion version) {
		super(name, flags, typeLinks, information, constraints, path, dataType, version);
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
	public FhirCardinality getCardinality() {
		return new FhirCardinality(min, max);
	}
	
	@Override
	public Optional<Integer> getMin() {
		return Optional.of(min);
	}
	
	public Integer expectMin() {
		return min;
	}
	
	@Override
	public Optional<String> getMax() {
		return Optional.of(max);
	}
	
	public String expectMax() {
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
