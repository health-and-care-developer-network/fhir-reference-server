package uk.nhs.fhir.render.tree.tidy;

import java.util.Optional;

import uk.nhs.fhir.data.structdef.SlicingInfo;

public interface HasSlicingInfo {
	public boolean hasSlicingInfo();
	public Optional<SlicingInfo> getSlicingInfo();
}
