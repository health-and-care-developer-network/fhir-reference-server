package uk.nhs.fhir.datalayer.collections;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceStatus;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;

public class ResourceEntityWithMultipleVersions {
	
	private static final Logger LOG = LoggerFactory.getLogger(ResourceEntityWithMultipleVersions.class.getName());

	Map<VersionNumber,ResourceMetadata> metadataByVersion = new TreeMap<VersionNumber,ResourceMetadata>(VersionNumber.BY_MAJOR_MINOR);
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
		if (metadataByVersion.containsKey(entity.getVersionNo())) {
			// resolve situation where two resources have matching major and minor versions
			int compareResult = entity.getVersionNo().compareTo(metadataByVersion.get(entity.getVersionNo()).getVersionNo());
			if (compareResult == 0) {
				// Two separate versioned files with identical id and patch. Something is wrong.
				throw new IllegalStateException("Found multiple versions of resource with id " + entity.getResourceID() + ": " 
				+ entity.getResourceFile().getAbsolutePath() +" and " + metadataByVersion.get(entity.getVersionNo()).getResourceFile().getAbsolutePath());
			} else if (compareResult < 0) {
				// new entity is lower value
				LOG.info("Ignoring resource at " + entity.getResourceFile().getAbsolutePath() + " since " + metadataByVersion.get(entity.getVersionNo()).getResourceFile().getAbsolutePath() + " has higher patch version");
				return;
			} else {
				// new entiry is higher value
				LOG.info("Resource at " + entity.getResourceFile().getAbsolutePath() + " will replace " + metadataByVersion.get(entity.getVersionNo()).getResourceFile().getAbsolutePath() + " since it has higher patch version");
			}
			
			// if we don't remove, we'll still display the old version number!
			metadataByVersion.remove(entity.getVersionNo());
			metadataByVersion.put(entity.getVersionNo(), entity);
		} else {
			metadataByVersion.put(entity.getVersionNo(), entity);
		}
		
		latest = largestVersion(latest, entity.getVersionNo());
		
		if (entity.getStatus().equals(ResourceStatus.active)) {
			latestActive = largestVersion(latestActive, entity.getVersionNo());
		} else if (entity.getStatus().equals(ResourceStatus.draft)) {
			latestDraft = largestVersion(latestDraft, entity.getVersionNo());
		}
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
		StringBuilder result = new StringBuilder("  - ResourceEntityWithMultipleVersions [ID=" + resourceID + ", latestVersion=" + latest + "] - Versions:");
		
		for (ResourceMetadata metadata : metadataByVersion.values()) {
			result.append("\n").append(metadata.toString());
		}
		
		return result.toString();
	}

	public Map<VersionNumber, ResourceMetadata> getVersionList() {
		return metadataByVersion;
	}
}
