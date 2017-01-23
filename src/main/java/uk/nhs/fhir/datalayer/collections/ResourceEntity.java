package uk.nhs.fhir.datalayer.collections;

import java.io.File;

import uk.nhs.fhir.enums.ResourceType;

public class ResourceEntity {
	private static String resourceName = null;
	private static File resourceFile = null;
	private static String group = null;
	private static ResourceType resourceType;
	
	public ResourceEntity(String resourceName, File resourceFile, ResourceType resourceType) {
		this.resourceName = resourceName;
		this.resourceFile = resourceFile;
		this.resourceType = resourceType;
	}
	
	public static String getResourceName() {
		return resourceName;
	}
	public static void setResourceName(String resourceName) {
		ResourceEntity.resourceName = resourceName;
	}
	public static File getResourceFile() {
		return resourceFile;
	}
	public static void setResourceFile(File resourceFile) {
		ResourceEntity.resourceFile = resourceFile;
	}
	public static String getGroup() {
		return group;
	}
	public static void setGroup(String group) {
		ResourceEntity.group = group;
	}
	
}
