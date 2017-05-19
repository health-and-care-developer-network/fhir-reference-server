package uk.nhs.fhir.datalayer.collections;

import java.util.HashMap;

public class ResourceEntityWithMultipleVersions implements Comparable {

	HashMap<VersionNumber,ResourceEntity> versionList = new HashMap<VersionNumber,ResourceEntity>();
	VersionNumber latest = null;
	VersionNumber latestActive = null;
	VersionNumber latestDraft = null;
	String resourceID = null;
	String resourceName = null;
	
	public ResourceEntityWithMultipleVersions(ResourceEntity entity) {
		this.resourceID = entity.getResourceID();
		this.resourceName = entity.getResourceName();
		add(entity);
	}
	
	public void add(ResourceEntity entity) {
		latest = largestVersion(latest, entity.getVersionNo());
		if (entity.getStatus().equals("active")) {
			latestActive = largestVersion(latestActive, entity.getVersionNo());
		} else if (entity.getStatus().equals("draft")) {
			latestDraft = largestVersion(latestDraft, entity.getVersionNo());
		}
		versionList.put(entity.getVersionNo(), entity);
	}
	
	public ResourceEntity getLatest() {
		return versionList.get(latest);
	}
	
	public ResourceEntity getSpecificVersion(VersionNumber version) {
		return versionList.get(version);
	}
	
	private VersionNumber largestVersion(VersionNumber previousLatest, VersionNumber newVersion) {
		if (previousLatest == null) {
			return newVersion;
		}
		if (newVersion.isValid()) {
			if (newVersion.compareTo(previousLatest) > 0) {
				// New version is bigger
				return newVersion;
			}
		}
		return previousLatest;
	}
	
	/**
	 * Allow resources to be sorted by name
	 */
	@Override
	public int compareTo(Object arg0) {
		ResourceEntityWithMultipleVersions other = (ResourceEntityWithMultipleVersions)arg0;
		return this.getLatest().compareTo(other.getLatest());
	}

	public String getResourceID() {
		return resourceID;
	}

	public String getResourceName() {
		return resourceName;
	}
	
	@Override
	public String toString() {
		String result = "  - ResourceEntityWithMultipleVersions [ID=" + resourceID + ", latestVersion=" + latest + "] - Versions:";
		for (VersionNumber version : versionList.keySet()) {
			result = result + "\n" + versionList.get(version).toString();
		}
		return result;
	}
}
