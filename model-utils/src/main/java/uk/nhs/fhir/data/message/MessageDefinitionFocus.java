package uk.nhs.fhir.data.message;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class MessageDefinitionFocus {

	private final String code;
	private final String structureDefinitionReference;
	private final String bundleExtensionUrl;
	private final List<MessageDefinitionAsset> assets = Lists.newArrayList();

	public MessageDefinitionFocus(String code, String structureDefinitionReference, String bundleExtensionUrl) {
		this.code = Preconditions.checkNotNull(code);
		this.structureDefinitionReference = Preconditions.checkNotNull(structureDefinitionReference); 
		this.bundleExtensionUrl = Preconditions.checkNotNull(bundleExtensionUrl);
	}

	public String getCode() {
		return code;
	}
	
	public String getStructureDefinitionReference() {
		return structureDefinitionReference;
	}
	
	public String getBundleExtensionUrl() {
		return bundleExtensionUrl;
	}
	
	public void addAsset(MessageDefinitionAsset asset) {
		assets.add(asset);
	}
	
	public List<MessageDefinitionAsset> getAssets() {
		return ImmutableList.copyOf(assets);
	}
}
