package uk.nhs.fhir.datalayer.collections;

import java.io.File;

import uk.nhs.fhir.enums.ResourceType;

public class ResourceEntity implements Comparable {
	private String resourceName = null;
	private String actualResourceName = null;
	private File resourceFile = null;
	private String group = null;
	private ResourceType resourceType;
	private boolean extension = false;
	private String baseType = null;
	private String displayGroup = null;
	private boolean example = false;
	
	public ResourceEntity(String resourceName, File resourceFile, ResourceType resourceType,
							boolean extension, String baseType, String displayGroup, boolean example,
							String actualResourceName) {
		this.resourceName = resourceName;
		this.resourceFile = resourceFile;
		this.resourceType = resourceType;
		this.extension = extension;
		this.baseType = baseType;
		this.displayGroup = displayGroup;
		this.example = example;
		this.actualResourceName = actualResourceName;
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

	public String getActualResourceName() {
		return actualResourceName;
	}

	public void setActualResourceName(String actualResourceName) {
		this.actualResourceName = actualResourceName;
	}

	@Override
	public int compareTo(Object arg0) {
		ResourceEntity other = (ResourceEntity)arg0;
		if (this.resourceName.equals(other.resourceName) && this.resourceType == other.resourceType) {
			return 0;
		} else {
			return this.resourceName.compareTo(other.resourceName);
		}
	}
}
