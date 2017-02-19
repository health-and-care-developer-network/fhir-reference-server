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

import static uk.nhs.fhir.enums.ResourceType.STRUCTUREDEFINITION;
import static uk.nhs.fhir.enums.ResourceType.VALUESET;
import static uk.nhs.fhir.enums.ResourceType.OPERATIONDEFINITION;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import uk.nhs.fhir.datalayer.collections.ResourceEntity;
import uk.nhs.fhir.enums.ResourceType;
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
    private static List<ResourceEntity> resourceList = null;
    
    private static long lastUpdated = 0;
    private static long updateInterval = Long.parseLong(PropertyReader.getProperty("cacheReloadIntervalMS"));
    private static String profilePath = PropertyReader.getProperty("profilePath");
    private static String valueSetPath = PropertyReader.getProperty("valusetPath");
    private static String operationsPath = PropertyReader.getProperty("operationsPath");
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
        ArrayList<String> names = new ArrayList<String>();
        for(ResourceEntity entry : resourceList) {
        	if (entry.getResourceType() == resourceType)
        		names.add(entry.getResourceName());
        }
        return names;
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
            for(ResourceEntity entry : resourceList) {
            	if (entry.getResourceType() == resourceType) {
	            	boolean isExtension = entry.isExtension();
	                //TODO: Show extensions differently?
	                String group = entry.getDisplayGroup();
	                String name = entry.getResourceName();
	                if(result.containsKey(group)) {
	                    List<ResourceEntity> resultEntry = result.get(group);
	                    resultEntry.add(entry);
	                } else {
	                    List<ResourceEntity> resultEntry = new ArrayList<ResourceEntity>();
	                    resultEntry.add(entry);
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
     * Method to get all of the ValueSets
     * 
     * @return 
     */
    public static List getResources(ResourceType resourceType) {
        if(updateRequired()) {
            updateCache();
        }
        // Load each resource file and put them in a list to return
        ArrayList allFiles = new ArrayList();
        for (ResourceEntity entry : resourceList) {
        	if (entry.getResourceType() == resourceType) {
        		IBaseResource vs = FHIRUtils.loadResourceFromFile(entry.getResourceFile());
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
            lastUpdated = System.currentTimeMillis();
            LOG.fine("Updating cache from filesystem");
            
            // Load StructureDefinitions
            ArrayList<ResourceEntity> newList = cacheFHIRResources(profilePath, STRUCTUREDEFINITION);
            
            // Add ValueSets
            newList.addAll(cacheFHIRResources(valueSetPath, VALUESET));
            
            // Add operations
            newList.addAll(cacheFHIRResources(operationsPath, OPERATIONDEFINITION));
            
            // Swap out for our new list
            resourceList = newList;
        }
    }
    
    private static ArrayList<ResourceEntity> cacheFHIRResources(String path, ResourceType resourceType){
        ArrayList<ResourceEntity> newFileList = new ArrayList<ResourceEntity>();
        File folder = new File(path);
            File[] fileList = folder.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(fileExtension);
                }
            });
            
        for (File thisFile : fileList) {
            if (thisFile.isFile()) {
                LOG.fine("Reading " + resourceType + " ResourceEntity into cache: " + thisFile.getName());
                
                String name = null;
                String actualName = null;
                boolean extension = false;
                String baseType = "Other";
                String displayGroup = null;
                boolean example = false;
                
                try {
	                if (resourceType == STRUCTUREDEFINITION) {
	                	StructureDefinition profile = (StructureDefinition)FHIRUtils.loadResourceFromFile(thisFile);
	                	name = profile.getName();
	                	extension = (profile.getBase().equals("http://hl7.org/fhir/StructureDefinition/Extension"));
	                    baseType = profile.getConstrainedType();
	                    actualName = getActualNameFromURL(profile.getUrl(), name);
	                    displayGroup = baseType;
	                } else if (resourceType == VALUESET) {
	                	displayGroup = "Code List";
	                	ValueSet profile = (ValueSet)FHIRUtils.loadResourceFromFile(thisFile);
	                	name = profile.getName();
	                	actualName = getActualNameFromURL(profile.getUrl(), name);
	                	if (FHIRUtils.isValueSetSNOMED(profile)) {
	                		displayGroup = "SNOMED CT Code List";
	                	}
	                } else if (resourceType == OPERATIONDEFINITION) {
	                	OperationDefinition operation = (OperationDefinition)FHIRUtils.loadResourceFromFile(thisFile);
	                	name = operation.getName();
	                	//extension = (operation.getBase().equals("http://hl7.org/fhir/StructureDefinition/Extension"));
	                    //baseType = operation.getType();
	                    actualName = getActualNameFromURL(operation.getUrl(), name);
	                    displayGroup = "Operations";
	                }
	                newFileList.add(new ResourceEntity(name, thisFile, resourceType, extension, baseType,
	                										displayGroup, example, actualName));
                } catch (Exception ex) {
                	LOG.severe("Unable to load FHIR resource from file: "+thisFile.getAbsolutePath() + " - IGNORING");
                }
            }
        }
        
        // Sort our collection into alpha order by resource name
        Collections.sort(newFileList);
        
        return newFileList;
    }
    
    private static String getActualNameFromURL(String url, String def) {
    	// Find the actual name of the resource from the URL
        int idx = url.lastIndexOf('/');
        if (idx > -1) {
        	return url.substring(idx+1);
        } else {
        	// Can't find a real name in the URL!
        	return def;
        }
    }
    
    public static ResourceEntity getSingleResourceByName(String name) {
        if(updateRequired()) {
            updateCache();
        }
    	for (ResourceEntity entry : resourceList) {
    		if (entry.getResourceName().equals(name) || entry.getActualResourceName().equals(name)) {
    			return entry;
    		}
    	}
    	return null;
    }

}
