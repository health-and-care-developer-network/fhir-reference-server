/*
 * Copyright (C) 2016 Health and Social Care Information Centre.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.nhs.fhir.datalayer;

import static uk.nhs.fhir.datalayer.DataLoaderMessages.addMessage;
import static uk.nhs.fhir.enums.ResourceType.IMPLEMENTATIONGUIDE;
import static uk.nhs.fhir.enums.ResourceType.OPERATIONDEFINITION;
import static uk.nhs.fhir.enums.ResourceType.STRUCTUREDEFINITION;
import static uk.nhs.fhir.enums.ResourceType.VALUESET;
import static uk.nhs.fhir.util.FHIRUtils.getResourceIDFromURL;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.dstu2.resource.ImplementationGuide;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import uk.nhs.fhir.datalayer.collections.ResourceEntity;
import uk.nhs.fhir.datalayer.collections.ResourceEntityWithMultipleVersions;
import uk.nhs.fhir.datalayer.collections.VersionNumber;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.util.DateUtils;
import uk.nhs.fhir.util.FHIRUtils;
import uk.nhs.fhir.util.PropertyReader;

/**
 * Holds an in-memory cache of metadata about FHIR resources loaded from the filesystem.
 *
 * @author Adam Hatherly
 */
public class FileCache {
    private static final Logger LOG = Logger.getLogger(FileCache.class.getName());

    // Singleton object to act as a cache of the files in the profiles and valueset directories
    private static List<ResourceEntityWithMultipleVersions> resourceList = null;
    
    private static long lastUpdated = 0;
    private static long updateInterval = Long.parseLong(PropertyReader.getProperty("cacheReloadIntervalMS"));
    private static String fileExtension = PropertyReader.getProperty("fileExtension");


    /**
     * Method to get the cached set of Resource names of the specified type
     * 
     * @return 
     */
    public static List<String> getResourceNameList(ResourceType resourceType) {
        if(updateRequired()) {
            updateCache();
        }
        HashSet<String> names = new HashSet<String>();
        for(ResourceEntityWithMultipleVersions entry : resourceList) {
        	if (entry.getLatest().getResourceType() == resourceType)
        		if (!names.contains(entry.getLatest().getResourceName()))
        			names.add(entry.getLatest().getResourceName());
        }
        ArrayList<String> nameList = new ArrayList(names);
        return nameList;
    }
    
    /**
     * Return resource list grouped into sensible groups
     * @param resourceType
     * @return
     */
    public static HashMap<String, List<ResourceEntity>> getGroupedNameList(ResourceType resourceType) {
        if(updateRequired()) {
            updateCache();
        }
        
        LOG.info("Creating HashMap");
        HashMap<String, List<ResourceEntity>> result = new HashMap<String, List<ResourceEntity>>();
        try {
            for(ResourceEntityWithMultipleVersions entry : resourceList) {
            	if (entry.getLatest().getResourceType() == resourceType) {
	            	boolean isExtension = entry.getLatest().isExtension();
	                //TODO: Show extensions differently?
	                String group = entry.getLatest().getDisplayGroup();
	                String name = entry.getLatest().getResourceName();
	                if(result.containsKey(group)) {
	                    List<ResourceEntity> resultEntry = result.get(group);
	                    resultEntry.add(entry.getLatest());
	                } else {
	                    List<ResourceEntity> resultEntry = new ArrayList<ResourceEntity>();
	                    resultEntry.add(entry.getLatest());
	                    result.put(group, resultEntry);
	                }
            	}
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.severe("ERROR: " + e.getMessage());
        }
        LOG.info("Generated HashMap: " + result.toString());
        return result;
    }
    
    
    /**
     * Method to get all of the resources of this type (latest versions)
     * 
     * @return 
     */
    public static List<IBaseResource> getResources(ResourceType resourceType) {
        if(updateRequired()) {
            updateCache();
        }
        // Load each resource file and put them in a list to return
        ArrayList<IBaseResource> allFiles = new ArrayList<IBaseResource>();
        for (ResourceEntityWithMultipleVersions entry : resourceList) {
        	if (entry.getLatest().getResourceType() == resourceType) {
        		IBaseResource vs = FHIRUtils.loadResourceFromFile(entry.getLatest().getResourceFile());
        		allFiles.add(vs);
        	}
        }
        return allFiles;
    }
    
    private static boolean updateRequired() {
        long currentTime = System.currentTimeMillis();
        if(resourceList == null || (currentTime > (lastUpdated + updateInterval))) {
            LOG.fine("Cache needs updating");
            return true;
        }
        LOG.info("Using Cache");
        return false;
    }
    
    private synchronized static void updateCache() {
        if(updateRequired()) {
        	DataLoaderMessages.clearProfileLoadMessages();
            lastUpdated = System.currentTimeMillis();
            LOG.fine("Updating cache from filesystem");
            
            // Load StructureDefinitions
            ArrayList<ResourceEntityWithMultipleVersions> newList = cacheFHIRResources(STRUCTUREDEFINITION);
            
            // Add ValueSets
            //newList.addAll(cacheFHIRResources(VALUESET));
            
            // Add operations
            //newList.addAll(cacheFHIRResources(OPERATIONDEFINITION));

            // Add ImplementationGuides
            //newList.addAll(cacheFHIRResources(IMPLEMENTATIONGUIDE));
            
            // Swap out for our new list
            resourceList = newList;
            printCacheContent();
        }
    }
    
    private static ArrayList<ResourceEntityWithMultipleVersions> cacheFHIRResources(ResourceType resourceType){
    	
    	// Call pre-processor to copy files into the versioned directory
    	try {
    		VersionedFilePreprocessor.copyFHIRResourcesIntoVersionedDirectory(resourceType);
    	} catch (IOException e) {
    		LOG.severe("Unable to pre-process files into versioned directory! - error: " + e.getMessage());
    	}
    	
    	LOG.info("Started loading resources into cache");
		addMessage("Started loading " + resourceType + " resources into cache");
    	
        // Now, read the resources from the versioned path into our cache
    	ArrayList<ResourceEntityWithMultipleVersions> newFileList = new ArrayList<ResourceEntityWithMultipleVersions>();
        String path = resourceType.getVersionedFilesystemPath();
        File folder = new File(path);
            File[] fileList = folder.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(fileExtension);
                }
            });
        
        if (fileList != null) {
	        for (File thisFile : fileList) {
	            if (thisFile.isFile()) {
	                LOG.fine("Reading " + resourceType + " ResourceEntity into cache: " + thisFile.getName());
	                
	                String name = null;
	                String resourceID = null;
	                boolean extension = false;
	                String baseType = "Other";
	                String displayGroup = null;
	                boolean example = false;
	                VersionNumber versionNo = null;
	                String status = null;
	                
	                try {
		                if (resourceType == STRUCTUREDEFINITION) {
		                	StructureDefinition profile = (StructureDefinition)FHIRUtils.loadResourceFromFile(thisFile);
		                	name = profile.getName();
		                	extension = (profile.getBase().equals("http://hl7.org/fhir/StructureDefinition/Extension"));
		                    baseType = profile.getConstrainedType();
		                    resourceID = getResourceIDFromURL(profile.getUrl(), name);
		                    displayGroup = baseType;
		                    versionNo = new VersionNumber(profile.getVersion());
		                    status = profile.getStatus();
		                } else if (resourceType == VALUESET) {
		                	displayGroup = "Code List";
		                	ValueSet profile = (ValueSet)FHIRUtils.loadResourceFromFile(thisFile);
		                	name = profile.getName();
		                	resourceID = getResourceIDFromURL(profile.getUrl(), name);
		                	if (FHIRUtils.isValueSetSNOMED(profile)) {
		                		displayGroup = "SNOMED CT Code List";
		                	}
		                	versionNo = new VersionNumber(profile.getVersion());
		                	status = profile.getStatus();
		                } else if (resourceType == OPERATIONDEFINITION) {
		                	OperationDefinition operation = (OperationDefinition)FHIRUtils.loadResourceFromFile(thisFile);
		                	name = operation.getName();
		                    resourceID = getResourceIDFromURL(operation.getUrl(), name);
		                    displayGroup = "Operations";
		                    versionNo = new VersionNumber(operation.getVersion());
		                    status = operation.getStatus();
		                } else if (resourceType == IMPLEMENTATIONGUIDE) {
		                	ImplementationGuide guide = (ImplementationGuide)FHIRUtils.loadResourceFromFile(thisFile);
		                	name = guide.getName();
		                    resourceID = getResourceIDFromURL(guide.getUrl(), name);
		                    displayGroup = "Implementation Guides";
		                    versionNo = new VersionNumber(guide.getVersion());
		                    status = guide.getStatus();
		                }
		                
		                ResourceEntity newEntity = new ResourceEntity(name, thisFile, resourceType, extension, baseType,
								displayGroup, example, resourceID, versionNo, status);
		                
		                addToResourceList(newFileList,newEntity);
		                
		                addMessage("  - Loading " + resourceType + " resource with ID: " + resourceID + " and version: " + versionNo);
	                } catch (Exception ex) {
	                	LOG.severe("Unable to load FHIR resource from file: "+thisFile.getAbsolutePath() + " - IGNORING");
	                	addMessage("[!] Error loading " + resourceType + " resource from file : " + thisFile.getAbsolutePath() + " message: " + ex.getMessage());
	                }
	            }
	        }
        }
        
        // Sort our collection into alpha order by resource name
        //Collections.sort(newFileList);
        LOG.fine("Finished reading resources into cache");
        return newFileList;
    }
    
    
    private static void addToResourceList(ArrayList<ResourceEntityWithMultipleVersions> list,
    										ResourceEntity entry) {
    	boolean found = false;
    	for (ResourceEntityWithMultipleVersions listItem : list) {
    		if (listItem.getResourceID().equals(entry.getResourceID())) {
    			// This is a new version of an existing resource - add the version
    			listItem.add(entry);
    			found = true;
    			LOG.info("Added new version to resource: " + entry.getResourceID());
    		}
    	}
		if (!found) {
			// This is a new resource we haven't seen before
			ResourceEntityWithMultipleVersions newEntry = new ResourceEntityWithMultipleVersions(entry);
			list.add(newEntry);
			LOG.info("Added new resource (first version found): " + entry.getResourceID());
		}
    }
    
    public static ResourceEntity getSingleResourceByID(String id) {
        if(updateRequired()) {
            updateCache();
        }
    	for (ResourceEntityWithMultipleVersions entry : resourceList) {
    		/*if (entry.getResourceName().equals(id) || entry.getResourceID().equals(id)) {
    			return entry;
    		}*/
    		if (entry.getResourceID().equals(id)) {
    			return entry.getLatest();
    		}
    	}
    	return null;
    }
    
    public static ResourceEntity getSingleResourceByName(String name) {
        if(updateRequired()) {
            updateCache();
        }
    	for (ResourceEntityWithMultipleVersions entry : resourceList) {
    		if (entry.getResourceName().equals(name)) {
    			return entry.getLatest();
    		}
    	}
    	return null;
    }

	public static List<ResourceEntity> getResourceList() {
		if(updateRequired()) {
            updateCache();
        }
		
		List<ResourceEntity> latestResourcesList = new ArrayList<ResourceEntity>();
		for (ResourceEntityWithMultipleVersions item : resourceList) {
			latestResourcesList.add(item.getLatest());
		}
		return latestResourcesList;
	}
    
    private static void printCacheContent() {
    	addMessage(" ===  ===  ===   Cache Contents   === === ===");
    	LOG.info("Cache loaded - entries:");
    	for (ResourceEntityWithMultipleVersions entry : resourceList) {
    		LOG.info("  -> " + entry);
    		addMessage(entry.toString());
    	}
    	addMessage(" ===  ===  ===  End Cache Contents === === ===");
    }
}
