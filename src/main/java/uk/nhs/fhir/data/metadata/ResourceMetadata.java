package uk.nhs.fhir.data.metadata;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import uk.nhs.fhir.util.FhirVersion;

public class ResourceMetadata {
	/**
	 * Allow resources to be sorted by name
	 */
	public static final Comparator<ResourceMetadata> BY_RESOURCE_NAME = new Comparator<ResourceMetadata>() {

		@Override
		public int compare(ResourceMetadata resource1, ResourceMetadata resource2) {
			if (resource1.resourceName.equals(resource2.resourceName) && resource1.resourceType == resource2.resourceType) {
				return 0;
			} else {
				return resource1.resourceName.compareTo(resource2.resourceName);
			}
		}
		
	};
	
	private String resourceName = null;
	private String resourceID = null;
	private File resourceFile = null;
	private ResourceType resourceType;
	private boolean extension = false;
	private Optional<String> baseType = null;
	private String displayGroup = null;
	private boolean example = false;
	private VersionNumber versionNo = null;
	private ResourceStatus status = null;
	List<SupportingArtefact> artefacts = null;
	private String extensionCardinality = null;
	List<String> extensionContexts = null;
	private String extensionDescription = null;
	private FhirVersion fhirVersion = null;
	private String url = null;
	
	/**
	 * Create some metadata for the resource
	 * @param resourceName
	 * @param resourceFile
	 * @param resourceType
	 * @param extension
	 * @param baseType
	 * @param displayGroup
	 * @param example
	 * @param resourceID
	 * @param versionNo
	 * @param status
	 * @param artefacts
	 * @param cardinality
	 * @param extensionContexts
	 * @param extensionDescription
	 * @param fhirVersion
	 * @param url
	 */
	public ResourceMetadata(String resourceName, File resourceFile, ResourceType resourceType,
							boolean extension, Optional<String> baseType, String displayGroup, boolean example,
							String resourceID, VersionNumber versionNo, String status,
							List<SupportingArtefact> artefacts, String cardinality,
							List<String> extensionContexts, String extensionDescription,
							FhirVersion fhirVersion, String url) {
		this.resourceName = resourceName;
		this.resourceFile = resourceFile;
		this.resourceType = resourceType;
		this.extension = extension;
		this.baseType = baseType;
		this.displayGroup = displayGroup;
		this.example = example;
		this.resourceID = resourceID;
		this.versionNo = versionNo;
		
		if (status == null) {
			this.status = null;
		} else {
			this.status = ResourceStatus.getStatus(status);
		}
		
		this.artefacts = artefacts;
		this.extensionCardinality = cardinality;
		this.extensionContexts = extensionContexts;
		this.extensionDescription = extensionDescription;
		this.fhirVersion = fhirVersion;
		this.setUrl(url);
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
	public boolean isExtension() {
		return extension;
	}
	public void setExtension(boolean extension) {
		this.extension = extension;
	}

	public String getBaseType() {
		return baseType.get();
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

	@Override
	public String toString() {
		int artefactCount = 0;
		if (artefacts != null) {
			artefactCount = artefacts.size();			
		}
		return "      ResourceEntity [ID=" + resourceID + ", version=" + versionNo + ", artefacts=" + artefactCount + "]";
	}

	public ResourceStatus getStatus() {
		return status;
	}

	public void setStatus(ResourceStatus status) {
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

	public List<SupportingArtefact> getArtefacts() {
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

	public List<String> getExtensionContexts() {
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

	public FhirVersion getFhirVersion() {
		return fhirVersion;
	}

	public void setFhirVersion(FhirVersion fhirVersion) {
		this.fhirVersion = fhirVersion;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getFhirVersionLabel() {
		String fhirVersion = getFhirVersion().toString();
		return "<span class='" + fhirVersion.toLowerCase() + "'>" + fhirVersion.toUpperCase() + "</span>";
	}
	
	public String getVersionedFileName() {
		return resourceID + "-" + fhirVersion.toString() + "-versioned-" + versionNo + ".xml";
	}
}
