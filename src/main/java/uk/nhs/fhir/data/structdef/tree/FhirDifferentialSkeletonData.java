package uk.nhs.fhir.data.structdef.tree;

import java.util.Optional;

import uk.nhs.fhir.data.structdef.SlicingInfo;
import uk.nhs.fhir.data.structdef.tree.tidy.HasSlicingInfo;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.data.wrap.WrappedElementDefinition;

public class FhirDifferentialSkeletonData implements HasSlicingInfo, HasId {

	private final Optional<String> id;
	private final ImmutableNodePath path;
	private final Optional<String> sliceName;
	private final Optional<SlicingInfo> slicingInfo;
	private final LinkDatas typeLinks;
	private final Optional<String> fixedValue;
	
	private final WrappedElementDefinition elementDefinition;
	
	public FhirDifferentialSkeletonData(
			Optional<String> id,
			ImmutableNodePath path, 
			Optional<SlicingInfo> slicingInfo, 
			Optional<String> sliceName, 
			LinkDatas typeLinks,
			Optional<String> fixedValue,
			WrappedElementDefinition elementDefinition) {
		this.id = id;
		this.path = path;
		this.slicingInfo = slicingInfo;
		this.sliceName = sliceName;
		this.typeLinks = typeLinks;
		this.fixedValue = fixedValue;
		this.elementDefinition = elementDefinition;
	}
	
	public ImmutableNodePath getPath() {
		return path;
	}
	
	public String getPathName() {
		return path.getPathName();
	}
	
	public String getPathString() {
		return path.toString();
	}

	public WrappedElementDefinition getElement() {
		return elementDefinition;
	}

	@Override
	public boolean hasSlicingInfo() {
		return getSlicingInfo().isPresent();
	}

	@Override
	public Optional<SlicingInfo> getSlicingInfo() {
		return slicingInfo;
	}
	
	public Optional<String> getSliceName() {
		return sliceName;
	}
	
	public LinkDatas getTypeLinks() {
		return typeLinks;
	}

	public Optional<String> getFixedValue() {
		return fixedValue;
	}

	@Override
	public Optional<String> getId() {
		return id;
	}

}
