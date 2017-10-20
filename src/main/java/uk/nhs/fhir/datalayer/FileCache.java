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

import static uk.nhs.fhir.data.metadata.ResourceType.EXAMPLES;
import static uk.nhs.fhir.datalayer.DataLoaderMessages.addMessage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.primitive.IdDt;
import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.datalayer.collections.ExampleResources;
import uk.nhs.fhir.datalayer.collections.ResourceEntityWithMultipleVersions;
import uk.nhs.fhir.datalayer.collections.ResourceFileFinder;
import uk.nhs.fhir.makehtml.FhirFileParser;
import uk.nhs.fhir.util.AbstractFhirFileLocator;
import uk.nhs.fhir.util.FHIRUtils;
import uk.nhs.fhir.util.FhirVersion;

/**
 * Holds an in-memory cache of metadata about FHIR resources loaded from the filesystem.
 *
 * @author Adam Hatherly
 */
public class FileCache {
    private static final Logger LOG = Logger.getLogger(FileCache.class.getName());

    private static final ResourceFileFinder resourceFileFinder = new ResourceFileFinder();
    private static final FhirFileParser parser = new FhirFileParser();
    
    private static AbstractFhirFileLocator fhirFileLocator = new PropertiesFhirFileLocator();
    private static final VersionedFilePreprocessor preprocessor = new VersionedFilePreprocessor(fhirFileLocator);
    public static void setVersionedFileLocator(AbstractFhirFileLocator versionedFileLocator) {
    	FileCache.fhirFileLocator = versionedFileLocator;
    	preprocessor.setFhirFileLocator(versionedFileLocator);
    }
    
    // Singleton object to act as a cache of the files in the profiles and valueset directories
    private static Map<FhirVersion, List<ResourceEntityWithMultipleVersions>> resourceListByFhirVersion = null;
    private static Map<FhirVersion, Map<String, ExampleResources>> examplesListByFhirVersion = null;
    private static Map<FhirVersion, Map<String, ResourceMetadata>> examplesListByName = null;
    
    private static boolean cacheNeedsUpdating = true;
    
    public static void invalidateCache() {
    	cacheNeedsUpdating = true;
    }
    
    public static void clearCache() {
    	resourceListByFhirVersion = null;
    	examplesListByFhirVersion = null;
    	examplesListByName = null;
    	cacheNeedsUpdating = true;
    }
    
    private static boolean updateRequired() {
        if (cacheNeedsUpdating) {
            LOG.fine("Cache needs updating");
            return true;
        } else {
        	LOG.fine("Using Cache");
        	return false;
        }
    }
    
    private synchronized static void updateCache() {
        if(updateRequired()) {
        	updateCacheForSpecificFhirVersion(FhirVersion.DSTU2);
        	updateCacheForSpecificFhirVersion(FhirVersion.STU3);
        	cacheNeedsUpdating = false;
        }
    }
    
    private static void updateCacheForSpecificFhirVersion(FhirVersion fhirVersion) {
    	DataLoaderMessages.clearProfileLoadMessages();
        LOG.fine("Updating cache from filesystem");
        
        List<ResourceEntityWithMultipleVersions> newResourcesList = cacheResources(fhirVersion);
        HashMap<String, ExampleResources> newExamplesList = cacheExamples(fhirVersion);
        
        updateResourcesList(fhirVersion, newResourcesList);
        updateExamplesList(fhirVersion, newExamplesList);
    }

	static List<ResourceEntityWithMultipleVersions> cacheResources(FhirVersion fhirVersion) {
		List<ResourceEntityWithMultipleVersions> newList = Lists.newCopyOnWriteArrayList();
        for (ResourceType resourceType : ResourceType.typesForFhirVersion(fhirVersion)) {
        	newList.addAll(cacheFHIRResources(fhirVersion, resourceType));
        }
		return newList;
	}
    
    private static ArrayList<ResourceEntityWithMultipleVersions> cacheFHIRResources(FhirVersion fhirVersion, ResourceType resourceType){
    	
    	// Call pre-processor to copy files into the versioned directory
    	try {
    		preprocessor.copyFHIRResourcesIntoVersionedDirectory(fhirVersion, resourceType);
    	} catch (IOException e) {
    		LOG.severe("Unable to pre-process files into versioned directory! - error: " + e.getMessage());
    	}
    	
    	LOG.fine("Started loading resources into cache");
		addMessage("Started loading " + resourceType + " resources into cache");
    	
        // Now, read the resources from the versioned path into our cache
    	ArrayList<ResourceEntityWithMultipleVersions> newFileList = new ArrayList<>();
    	String sourcePathForResourceAndVersion = fhirFileLocator.getDestinationPathForResourceType(resourceType, fhirVersion).toString();
    	LOG.fine("Reading pre-processed files from path: " + sourcePathForResourceAndVersion);
        List<File> fileList = resourceFileFinder.findFiles(sourcePathForResourceAndVersion);
        
        for (File thisFile : fileList) {
            if (thisFile.isFile()) {
                LOG.fine("Reading " + resourceType + " ResourceEntity into cache: " + thisFile.getName());
                
                try {
                	WrappedResource<?> wrappedResource = WrappedResource.fromBaseResource(parser.parseFile(thisFile));
                	
                	if (wrappedResource.getImplicitFhirVersion().equals(fhirVersion)) {
    					ResourceMetadata newEntity = wrappedResource.getMetadata(thisFile);
    	                
    	                addToResourceList(newFileList,newEntity);
    	                
    	                addMessage("  - Loading " + resourceType + " resource with ID: " + newEntity.getResourceID() + " and version: " + newEntity.getVersionNo());
                	}

                } catch (Exception ex) {
                	LOG.severe("Unable to load FHIR resource from file: "+thisFile.getAbsolutePath() + " - IGNORING");
                	addMessage("[!] Error loading " + resourceType + " resource from file : " + thisFile.getAbsolutePath() + " message: " + ex.getMessage());
                	ex.printStackTrace();
                }
            }
        }
        
        // Sort our collection into alpha order by resource name
        //Collections.sort(newFileList);
        LOG.fine("Finished reading resources into cache");
        return newFileList;
    }
    
    private static void addToResourceList(ArrayList<ResourceEntityWithMultipleVersions> list,
    										ResourceMetadata entry) {
    	boolean found = false;
    	for (ResourceEntityWithMultipleVersions listItem : list) {
    		if (listItem.getResourceID().equals(entry.getResourceID())) {
    			// This is a new version of an existing resource - add the version
    			listItem.add(entry);
    			found = true;
    			LOG.fine("Added new version to resource: " + entry.getResourceID());
    		}
    	}
		if (!found) {
			// This is a new resource we haven't seen before
			ResourceEntityWithMultipleVersions newEntry = new ResourceEntityWithMultipleVersions(entry);
			list.add(newEntry);
			LOG.fine("Added new resource (first version found): " + entry.getResourceID());
		}
    }
    
    private static HashMap<String, ExampleResources> cacheExamples(FhirVersion fhirVersion){
    	
    	LOG.fine("Started loading example resources into cache");
		addMessage("Started loading example resources into cache");
    	
        // Now, read the resources into our cache
		HashMap<String, ExampleResources> examplesList = new HashMap<String, ExampleResources>();
        String path = fhirFileLocator.getDestinationPathForResourceType(EXAMPLES, fhirVersion).toString();
        List<File> fileList = resourceFileFinder.findFiles(path);
        
        for (File thisFile : fileList) {
            if (thisFile.isFile()) {
                LOG.fine("Reading example ResourceEntity into cache: " + thisFile.getName());
                
                String resourceID = null;
                
                try {
                	IResource exampleResource = (IResource)FHIRUtils.loadResourceFromFile(fhirVersion, thisFile);
                	IdDt id = exampleResource.getId();
                    resourceID = id.getIdPart();
                    
                    // Find the profile resource ID the example relates to
                    List<? extends IPrimitiveType<String>> profiles = exampleResource.getMeta().getProfile();
                    if (profiles.isEmpty()) {
                    	LOG.severe("Unable to load FHIR example resource from file: "+thisFile.getAbsolutePath() + " - no profile was specified in the example!");
                		addMessage("[!] Error loading example resource from file : " + thisFile.getAbsolutePath() + " no profile was specified in the example!");
                    }
                    for (IPrimitiveType<String> profile : profiles) {
                    	String profileStr = profile.getValueAsString();
                    	if (profileStr != null) {
                    		if (profileStr.contains("_history")) {
                    			LOG.severe("Unable to load FHIR example resource from file: "+thisFile.getAbsolutePath() + " - versioned profile URLs not supported!");
                    			addMessage("[!] Error loading example resource from file : " + thisFile.getAbsolutePath() + " versioned profile URLs not supported!");
                    		} else {
                    			String[] profileParts = profileStr.split("/");
                    			if (profileParts.length < 3) {
                    				LOG.severe("Unable to load FHIR example resource from file: "+thisFile.getAbsolutePath() + " - profile URL invalid: " + profileStr);
	                    			addMessage("[!] Error loading example resource from file : " + thisFile.getAbsolutePath() + " - profile URL invalid: " + profileStr);
                    			} else {
                    				// We seem to have a valid profile - add to our cache
            	                    String profileResourceID = profileParts[profileParts.length-2] + "/" + 
            	                    							profileParts[profileParts.length-1];

            	                    // Load the examples into a different in-memory cache for later look-up
            	                    ResourceMetadata newEntity = new ResourceMetadata(thisFile.getName(), thisFile, EXAMPLES, false, Optional.empty(),
            								null, true, resourceID, null, null, null, null, null, null, fhirVersion, null);
            		                
            	                    if (examplesList.containsKey(profileResourceID)) {
            	                    	examplesList.get(profileResourceID).add(newEntity);
            	                    } else {
            	                    	ExampleResources e = new ExampleResources();
            	                    	e.add(newEntity);
            	                    	examplesList.put(profileResourceID, e);
            	                    }
            	                    
            		                addMessage("  - Loading example resource with ID: " + resourceID + " as an example of resource with ID: " + profileResourceID);
                    			}
                    		}
                    	} else {
                    		LOG.warning("Unable to load FHIR example resource from file: "+thisFile.getAbsolutePath() + " - no profile was specified in the example!");
                    		addMessage("[!] Error loading example resource from file : " + thisFile.getAbsolutePath() + " no profile was specified in the example!");
                    	}
                    }
	                
                } catch (Exception ex) {
                	LOG.severe("Unable to load FHIR example resource from file: "+thisFile.getAbsolutePath() + " - IGNORING");
                	addMessage("[!] Error loading example resource from file : " + thisFile.getAbsolutePath() + " message: " + ex.getMessage());
                }
            }
        }
        LOG.fine("Finished reading example resources into cache");
        return examplesList;
    }

	static void updateResourcesList(FhirVersion fhirVersion, List<ResourceEntityWithMultipleVersions> newResourcesList) {
		// Swap out for our new list
        ensureResourceListByFhirVersion();
        resourceListByFhirVersion.put(fhirVersion, newResourcesList);
	}

	static void ensureResourceListByFhirVersion() {
		if (resourceListByFhirVersion == null) {
        	resourceListByFhirVersion = Maps.newConcurrentMap();
        }
	}

	static void updateExamplesList(FhirVersion fhirVersion, Map<String, ExampleResources> newExamplesList) {
		ensureExamplesListByFhirVersionExists(fhirVersion);
        examplesListByFhirVersion.put(fhirVersion, newExamplesList);
        
        ensureExamplesListByNameExists();
        examplesListByName.put(fhirVersion, buildExampleListByName(newExamplesList));
	}

	static void ensureExamplesListByFhirVersionExists(FhirVersion fhirVersion) {
		if (examplesListByFhirVersion == null) {
        	examplesListByFhirVersion = Maps.newConcurrentMap();
        }
	}

	static void ensureExamplesListByNameExists() {
		if (examplesListByName == null) {
        	examplesListByName = Maps.newConcurrentMap();
        }
	}
    
    /**
     * Takes the in-memory index used for finding examples for specific profile IDs and flips it to
     * return an index keyed on the example filename
     * @param oldList
     * @return
     */
    private static Map<String, ResourceMetadata> buildExampleListByName(Map<String, ExampleResources> oldList) {
    	Map<String, ResourceMetadata> newList = Maps.newConcurrentMap();
    	for (String key : oldList.keySet()) {
    		ExampleResources examples = oldList.get(key);
    		for (ResourceMetadata example : examples) {
    			newList.put(example.getResourceName(), example);
    		}
    	}
    	return newList;
    }

    
    
    
    
    
    
    /**
     * Method to get the cached set of Resource names of the specified type
     * 
     * @return 
     */
    public static List<String> getResourceNameList(FhirVersion fhirVersion, ResourceType resourceType) {
        if(updateRequired()) {
            updateCache();
        }
        HashSet<String> names = new HashSet<String>();
        for(ResourceEntityWithMultipleVersions entry : resourceListByFhirVersion.get(fhirVersion)) {
        	if (entry.getLatest().getResourceType() == resourceType)
        		if (!names.contains(entry.getLatest().getResourceName()))
        			names.add(entry.getLatest().getResourceName());
        }
        ArrayList<String> nameList = new ArrayList<>(names);
        return nameList;
    }
    
    /**
     * Get a list of all extensions to show in the extensions registry
     * @return
     */
    public static List<ResourceMetadata> getExtensions(FhirVersion fhirVersion)  {
    	List<ResourceMetadata> results = new ArrayList<>();
    	if(updateRequired()) {
            updateCache();
        }
		for(ResourceEntityWithMultipleVersions entry : resourceListByFhirVersion.get(fhirVersion)) {
        	if (entry.getLatest().isExtension())
        		results.add(entry.getLatest());
        }
		Collections.sort(results, ResourceMetadata.BY_RESOURCE_NAME);
        return results;
	}
    
    
    /**
     * Return resource list grouped into sensible groups
     * @param resourceType
     * @return
     */
    public static HashMap<String, List<ResourceMetadata>> getGroupedNameList(ResourceType resourceType) {
        if(updateRequired()) {
            updateCache();
        }
        
        LOG.fine("Creating HashMap");
        HashMap<String, List<ResourceMetadata>> result = new HashMap<String, List<ResourceMetadata>>();
        try {
            for (FhirVersion fhirVersion : FhirVersion.getSupportedVersions()) {
	        	for(ResourceEntityWithMultipleVersions entry : resourceListByFhirVersion.get(fhirVersion)) {
	            	if (entry.getLatest().getResourceType() == resourceType) {
		            	boolean isExtension = entry.getLatest().isExtension();
		                String group = entry.getLatest().getDisplayGroup();
		                
		                // Don't include extensions
		                if (!isExtension) {
			                if(result.containsKey(group)) {
			                    List<ResourceMetadata> resultEntry = result.get(group);
			                    resultEntry.add(entry.getLatest());
			                } else {
			                    List<ResourceMetadata> resultEntry = new ArrayList<>();
			                    resultEntry.add(entry.getLatest());
			                    result.put(group, resultEntry);
			                }
		                }
	            	}
	            }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.severe("ERROR: " + e.getMessage());
        }
        LOG.fine("Generated HashMap: " + result.toString());
        return result;
    }
    
    
    /**
     * Method to get all of the resources of this type (latest versions)
     * 
     * @return 
     */
    public static List<IBaseResource> getResources(FhirVersion fhirVersion, ResourceType resourceType,
    												int theFromIndex, int theToIndex) {
        if(updateRequired()) {
            updateCache();
        }
        // Load each resource file and put them in a list to return
        int counter = 0;
        ArrayList<IBaseResource> allFiles = new ArrayList<>();
        for (ResourceEntityWithMultipleVersions entry : resourceListByFhirVersion.get(fhirVersion)) {
        	if (entry.getLatest().getResourceType() == resourceType) {
        		if (counter >= theFromIndex && counter < theToIndex) {
	        		IBaseResource vs = FHIRUtils.loadResourceFromFile(fhirVersion, entry.getLatest().getResourceFile());
	        		allFiles.add(vs);
        		}
        		counter++;
        	}
        }
        return allFiles;
    }
    
    public static ResourceMetadata getSingleResourceByID(FhirVersion fhirVersion, String idPart, String versionPart) {
        if(updateRequired()) {
            updateCache();
        }
        
    	for (ResourceEntityWithMultipleVersions entry : resourceListByFhirVersion.get(fhirVersion)) {
    		if (entry.getResourceID().equals(idPart)) {
    			if (versionPart != null) {
    				// Get a specific version
    				VersionNumber version = new VersionNumber(versionPart);
    				LOG.fine("Getting versioned resource with ID="+idPart + " and version="+version);
    				return entry.getSpecificVersion(version);
    			} else {
    				// Get the latest
    				return entry.getLatest();
    			}
    		}
    	}
    	return null;
    }
    
    public static ResourceEntityWithMultipleVersions getversionsByID(FhirVersion fhirVersion, String idPart, String versionPart) {
        if(updateRequired()) {
            updateCache();
        }
        
    	for (ResourceEntityWithMultipleVersions entry : resourceListByFhirVersion.get(fhirVersion)) {
    		if (entry.getResourceID().equals(idPart)) {
    			return entry;
    		}
    	}
    	return null;
    }
    
    public static ResourceMetadata getSingleResourceByName(FhirVersion fhirVersion, String name) {
        if(updateRequired()) {
            updateCache();
        }
    	for (ResourceEntityWithMultipleVersions entry : resourceListByFhirVersion.get(fhirVersion)) {
    		if (entry.getResourceName().equals(name)) {
    			return entry.getLatest();
    		}
    	}
    	return null;
    }

	public static List<ResourceMetadata> getResourceList(FhirVersion fhirVersion) {
		if(updateRequired()) {
            updateCache();
        }
		
		List<ResourceMetadata> latestResourcesList = new ArrayList<>();
		for (ResourceEntityWithMultipleVersions item : resourceListByFhirVersion.get(fhirVersion)) {
			latestResourcesList.add(item.getLatest());
		}
		return latestResourcesList;
	}
	
	
	
	
	public static ExampleResources getExamples(FhirVersion fhirVersion, String resourceTypeAndID) {
		if(updateRequired()) {
            updateCache();
        }
		return examplesListByFhirVersion.get(fhirVersion).get(resourceTypeAndID);
	}
	
	public static ResourceMetadata getExampleByName(FhirVersion fhirVersion, String resourceFilename) {
		if(updateRequired()) {
            updateCache();
        }
		return examplesListByName.get(fhirVersion).get(resourceFilename);
	}
}
