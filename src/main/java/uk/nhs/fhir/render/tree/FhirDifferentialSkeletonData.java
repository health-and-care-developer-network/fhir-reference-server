package uk.nhs.fhir.render.tree;

import java.util.Optional;

import uk.nhs.fhir.data.structdef.SlicingInfo;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.data.wrap.WrappedElementDefinition;
import uk.nhs.fhir.render.tree.tidy.HasSlicingInfo;

public class FhirDifferentialSkeletonData implements HasSlicingInfo {

	private final String path;
	private final Optional<String> sliceName;
	private final Optional<SlicingInfo> slicingInfo;
	private final LinkDatas typeLinks;
	private final Optional<String> fixedValue;
	
	private final WrappedElementDefinition elementDefinition;
	
	public FhirDifferentialSkeletonData(
			String path, 
			Optional<SlicingInfo> slicingInfo, 
			Optional<String> sliceName, 
			LinkDatas typeLinks,
			Optional<String> fixedValue,
			WrappedElementDefinition elementDefinition) {
		this.path = path;
		this.slicingInfo = slicingInfo;
		this.sliceName = sliceName;
		this.typeLinks = typeLinks;
		this.fixedValue = fixedValue;
		this.elementDefinition = elementDefinition;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getPathName() {
		String[] pathTokens = path.split("\\.");
		return pathTokens[pathTokens.length - 1];
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

}
