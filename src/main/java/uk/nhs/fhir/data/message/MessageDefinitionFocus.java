package uk.nhs.fhir.data.message;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class MessageDefinitionFocus {

	private final String profileId;
	private final String bundleExtensionUrl;
	private final List<MessageDefinitionAsset> assets = Lists.newArrayList();

	public MessageDefinitionFocus(String profileId, String bundleExtensionUrl) {
		this.profileId = Preconditions.checkNotNull(profileId);
		this.bundleExtensionUrl = Preconditions.checkNotNull(bundleExtensionUrl);
	}

	public String getProfileId() {
		return profileId;
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
