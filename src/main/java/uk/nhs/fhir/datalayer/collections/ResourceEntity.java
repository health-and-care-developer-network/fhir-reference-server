package uk.nhs.fhir.datalayer.collections;

import java.io.File;
import java.util.ArrayList;

import uk.nhs.fhir.enums.ResourceType;

public class ResourceEntity implements Comparable<ResourceEntity> {
	private String resourceName = null;
	private String resourceID = null;
	private File resourceFile = null;
	private String group = null;
	private ResourceType resourceType;
	private boolean extension = false;
	private String baseType = null;
	private String displayGroup = null;
	private boolean example = false;
	private VersionNumber versionNo = null;
	private String status = null;
	ArrayList<SupportingArtefact> artefacts = null;
	private String extensionCardinality = null;
	ArrayList<String> extensionContexts = null;
	private String extensionDescription = null;
	
	public ResourceEntity(String resourceName, File resourceFile, ResourceType resourceType,
							boolean extension, String baseType, String displayGroup, boolean example,
							String resourceID, VersionNumber versionNo, String status,
							ArrayList<SupportingArtefact> artefacts, String cardinality,
							ArrayList<String> extensionContexts, String extensionDescription) {
		this.resourceName = resourceName;
		this.resourceFile = resourceFile;
		this.resourceType = resourceType;
		this.extension = extension;
		this.baseType = baseType;
		this.displayGroup = displayGroup;
		this.example = example;
		this.resourceID = resourceID;
		this.versionNo = versionNo;
		this.status = status;
		this.artefacts = artefacts;
		this.extensionCardinality = cardinality;
		this.extensionContexts = extensionContexts;
		this.extensionDescription = extensionDescription;
		
	}
	
	public String getResourceName() {
		return resourceName;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	public File getResourceFile() {
		return resourceFile;
	}
	public void setResourceFile(File resourceFile) {
		this.resourceFile = resourceFile;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public boolean isExtension() {
		return extension;
	}
	public void setExtension(boolean extension) {
		this.extension = extension;
	}

	public String getBaseType() {
		return baseType;
	}

	public void setBaseType(String baseType) {
		this.baseType = baseType;
	}

	public String getDisplayGroup() {
		return displayGroup;
	}

	public void setDisplayGroup(String displayGroup) {
		this.displayGroup = displayGroup;
	}

	public ResourceType getResourceType() {
		return resourceType;
	}

	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

	public boolean isExample() {
		return example;
	}

	public void setExample(boolean example) {
		this.example = example;
	}

	public String getResourceID() {
		return resourceID;
	}

	public void setResourceID(String resourceID) {
		this.resourceID = resourceID;
	}
	
	public VersionNumber getVersionNo() {
		return versionNo;
	}

	public void setVersionNo(VersionNumber versionNo) {
		this.versionNo = versionNo;
	}

	/**
	 * Allow resources to be sorted by name
	 */
	@Override
	public int compareTo(ResourceEntity other) {
		if (this.resourceName.equals(other.resourceName) && this.resourceType == other.resourceType) {
			return 0;
		} else {
			return this.resourceName.compareTo(other.resourceName);
		}
	}

	@Override
	public String toString() {
		int artefactCount = 0;
		if (artefacts != null) {
			artefactCount = artefacts.size();			
		}
		return "      ResourceEntity [ID=" + resourceID + ", version=" + versionNo + ", artefacts=" + artefactCount + "]";
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getVersionedUrl(String baseURL) {
		StringBuilder url = new StringBuilder();
    	url.append(baseURL).append("/");
    	url.append(getResourceType().getHAPIName());
    	url.append("/").append(getResourceID());
    	url.append("/_history/").append(getVersionNo());
    	return url.toString();
	}

	public ArrayList<SupportingArtefact> getArtefacts() {
		return artefacts;
	}

	public void setArtefacts(ArrayList<SupportingArtefact> artefacts) {
		this.artefacts = artefacts;
	}

	public String getExtensionCardinality() {
		return extensionCardinality;
	}

	public void setExtensionCardinality(String extensionCardinality) {
		this.extensionCardinality = extensionCardinality;
	}

	public ArrayList<String> getExtensionContexts() {
		return extensionContexts;
	}

	public void setExtensionContexts(ArrayList<String> extensionContexts) {
		this.extensionContexts = extensionContexts;
	}

	public String getExtensionDescription() {
		return extensionDescription;
	}

	public void setExtensionDescription(String extensionDescription) {
		this.extensionDescription = extensionDescription;
	}
}
