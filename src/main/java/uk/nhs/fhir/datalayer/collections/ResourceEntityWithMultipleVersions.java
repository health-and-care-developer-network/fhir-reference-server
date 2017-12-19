package uk.nhs.fhir.datalayer.collections;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;

public class ResourceEntityWithMultipleVersions implements Comparable<ResourceEntityWithMultipleVersions> {
	
	private static final Logger LOG = LoggerFactory.getLogger(ResourceEntityWithMultipleVersions.class.getName());

	HashMap<VersionNumber,ResourceMetadata> metadataByVersion = new HashMap<VersionNumber,ResourceMetadata>();
	VersionNumber latest = null;
	VersionNumber latestActive = null;
	VersionNumber latestDraft = null;
	String resourceID = null;
	String resourceName = null;
	ResourceType resourceType = null;
	
	public ResourceEntityWithMultipleVersions(ResourceMetadata entity) {
		this.resourceID = entity.getResourceID();
		this.resourceName = entity.getResourceName();
		this.resourceType = entity.getResourceType();
		
		add(entity);
	}
	
	public void add(ResourceMetadata entity) {
		latest = largestVersion(latest, entity.getVersionNo());
		
		if (entity.getStatus().equals("active")) {
			latestActive = largestVersion(latestActive, entity.getVersionNo());
		} else if (entity.getStatus().equals("draft")) {
			latestDraft = largestVersion(latestDraft, entity.getVersionNo());
		}
		
		metadataByVersion.put(entity.getVersionNo(), entity);
	}
	
	public ResourceMetadata getLatest() {
		return metadataByVersion.get(latest);
	}
	
	public ResourceMetadata getSpecificVersion(VersionNumber version) {
		if (metadataByVersion.containsKey(version)) {
			LOG.debug("Found requested version - returning");
			return metadataByVersion.get(version);
		} else {
			LOG.warn("Could not find requested version - asked for version:" + version + " - versions we have are:");
			for (VersionNumber v : metadataByVersion.keySet()) {
				LOG.warn(" - version:" + v.toString());
			}
			return null;
		}
	}
	
	private VersionNumber largestVersion(VersionNumber previousLatest, VersionNumber newVersion) {
		if (previousLatest == null) {
			return newVersion;
		}
		
		boolean newVersionIsBigger = newVersion.compareTo(previousLatest) > 0; 
		
		if (newVersionIsBigger) {
			return newVersion;
		} else {
			return previousLatest;
		}
	}
	
	/**
	 * Allow resources to be sorted by name
	 */
	@Override
	public int compareTo(ResourceEntityWithMultipleVersions arg0) {
		ResourceEntityWithMultipleVersions other = (ResourceEntityWithMultipleVersions)arg0;
		return ResourceMetadata.BY_RESOURCE_NAME.compare(this.getLatest(), other.getLatest());
	}

	public String getResourceID() {
		return resourceID;
	}

	public String getResourceName() {
		return resourceName;
	}

	public ResourceType getResourceType() {
		return resourceType;
	}
	
	@Override
	public String toString() {
		String result = "  - ResourceEntityWithMultipleVersions [ID=" + resourceID + ", latestVersion=" + latest + "] - Versions:";
		for (VersionNumber version : metadataByVersion.keySet()) {
			result = result + "\n" + metadataByVersion.get(version).toString();
		}
		return result;
	}

	public HashMap<VersionNumber, ResourceMetadata> getVersionList() {
		return metadataByVersion;
	}
}
