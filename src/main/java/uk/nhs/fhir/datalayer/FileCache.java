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
import static uk.nhs.fhir.enums.ResourceType.EXAMPLES;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.primitive.IdDt;
import uk.nhs.fhir.datalayer.collections.ExampleResources;
import uk.nhs.fhir.datalayer.collections.ResourceEntityWithMultipleVersions;
import uk.nhs.fhir.datalayer.collections.ResourceFileFinder;
import uk.nhs.fhir.datalayer.collections.ResourceMetadata;
import uk.nhs.fhir.datalayer.collections.SupportingArtefact;
import uk.nhs.fhir.datalayer.collections.SupportingArtefact.OrderByWeight;
import uk.nhs.fhir.datalayer.collections.VersionNumber;
import uk.nhs.fhir.enums.ArtefactType;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.resourcehandlers.IResourceHelper;
import uk.nhs.fhir.resourcehandlers.ResourceHelperFactory;
import uk.nhs.fhir.util.FHIRUtils;
import uk.nhs.fhir.util.FHIRVersion;
import uk.nhs.fhir.util.FhirServerProperties;
import uk.nhs.fhir.util.FileLoader;

/**
 * Holds an in-memory cache of metadata about FHIR resources loaded from the filesystem.
 *
 * @author Adam Hatherly
 */
public class FileCache {
    private static final Logger LOG = Logger.getLogger(FileCache.class.getName());

    private static final ResourceFileFinder resourceFileFinder = new ResourceFileFinder();
    
    // Singleton object to act as a cache of the files in the profiles and valueset directories
    private static HashMap<FHIRVersion, List<ResourceEntityWithMultipleVersions>> resourceListByFhirVersion = null;
    private static HashMap<FHIRVersion, HashMap<String, ExampleResources>> examplesListByFhirVersion = null;
    private static HashMap<FHIRVersion, HashMap<String, ResourceMetadata>> examplesListByName = null;
    
    private static long lastUpdated = 0;
    private static long updateInterval = Long.parseLong(FhirServerProperties.getProperty("cacheReloadIntervalMS"));


    /**
     * Method to get the cached set of Resource names of the specified type
     * 
     * @return 
     */
    public static List<String> getResourceNameList(FHIRVersion fhirVersion, ResourceType resourceType) {
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
    public static List<ResourceMetadata> getExtensions(FHIRVersion fhirVersion)  {
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
            for (FHIRVersion fhirVersion : FHIRVersion.values()) {
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
    public static List<IBaseResource> getResources(FHIRVersion fhirVersion, ResourceType resourceType,
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
    
    private static boolean updateRequired() {
        long currentTime = System.currentTimeMillis();
        if(resourceListByFhirVersion == null || (currentTime > (lastUpdated + updateInterval))) {
            LOG.fine("Cache needs updating");
            return true;
        }
        LOG.fine("Using Cache");
        return false;
    }
    
    private synchronized static void updateCache() {
        if(updateRequired()) {
        	updateCacheForSpecificFHIRVersion(FHIRVersion.DSTU2);
        	updateCacheForSpecificFHIRVersion(FHIRVersion.STU3);
        }
    }
    
    private static void updateCacheForSpecificFHIRVersion(FHIRVersion fhirVersion) {
    	DataLoaderMessages.clearProfileLoadMessages();
        LOG.fine("Updating cache from filesystem");
        
        lastUpdated = System.currentTimeMillis();
        
        ArrayList<ResourceEntityWithMultipleVersions> newResourcesList = cacheResources(fhirVersion);
        HashMap<String, ExampleResources> newExamplesList = cacheExamples(fhirVersion);
        
        updateResourcesList(fhirVersion, newResourcesList);
        updateExamplesList(fhirVersion, newExamplesList);
        
        //printCacheContent(fhirVersion);
    }

	static void updateExamplesList(FHIRVersion fhirVersion, HashMap<String, ExampleResources> newExamplesList) {
		ensureExamplesListByFhirVersionExists(fhirVersion, newExamplesList);
        examplesListByFhirVersion.put(fhirVersion, newExamplesList);
        
        ensureExamplesListByNameExists();
        examplesListByName.put(fhirVersion, buildExampleListByName(newExamplesList));
	}

	static void ensureExamplesListByNameExists() {
		if (examplesListByName == null) {
        	examplesListByName = new HashMap<FHIRVersion, HashMap<String, ResourceMetadata>>();
        }
	}

	static void ensureExamplesListByFhirVersionExists(FHIRVersion fhirVersion,
			HashMap<String, ExampleResources> newExamplesList) {
		if (examplesListByFhirVersion == null) {
        	examplesListByFhirVersion = new HashMap<FHIRVersion, HashMap<String, ExampleResources>>();
        }
	}

	static void updateResourcesList(FHIRVersion fhirVersion,
			ArrayList<ResourceEntityWithMultipleVersions> newResourcesList) {
		// Swap out for our new list
        ensureResourceListByFhirVersion();
        resourceListByFhirVersion.put(fhirVersion, newResourcesList);
	}

	static void ensureResourceListByFhirVersion() {
		if (resourceListByFhirVersion == null) {
        	resourceListByFhirVersion = new HashMap<FHIRVersion, List<ResourceEntityWithMultipleVersions>>();
        }
	}

	static ArrayList<ResourceEntityWithMultipleVersions> cacheResources(FHIRVersion fhirVersion) {
		ArrayList<ResourceEntityWithMultipleVersions> newList = new ArrayList<>();
        for (ResourceType resourceType : ResourceType.typesForFhirVersion(fhirVersion)) {
        	newList.addAll(cacheFHIRResources(fhirVersion, resourceType));
        }
		return newList;
	}
    
    /**
     * Takes the in-memory index used for finding examples for specific profile IDs and flips it to
     * return an index keyed on the example filename
     * @param oldList
     * @return
     */
    private static HashMap<String, ResourceMetadata> buildExampleListByName(HashMap<String, ExampleResources> oldList) {
    	HashMap<String, ResourceMetadata> newList = new HashMap<String, ResourceMetadata>();
    	for (String key : oldList.keySet()) {
    		ExampleResources examples = oldList.get(key);
    		for (ResourceMetadata example : examples) {
    			newList.put(example.getResourceName(), example);
    		}
    	}
    	return newList;
    }
    
    private static ArrayList<ResourceEntityWithMultipleVersions> cacheFHIRResources(FHIRVersion fhirVersion, ResourceType resourceType){
    	
    	// Call pre-processor to copy files into the versioned directory
    	try {
    		VersionedFilePreprocessor.copyFHIRResourcesIntoVersionedDirectory(fhirVersion, resourceType);
    	} catch (IOException e) {
    		LOG.severe("Unable to pre-process files into versioned directory! - error: " + e.getMessage());
    	}
    	
    	LOG.fine("Started loading resources into cache");
		addMessage("Started loading " + resourceType + " resources into cache");
    	
        // Now, read the resources from the versioned path into our cache
    	ArrayList<ResourceEntityWithMultipleVersions> newFileList = new ArrayList<>();
    	String path = resourceType.getVersionedFilesystemPath(fhirVersion);
    	LOG.fine("Reading pre-processed files from path: " + path);
        List<File> fileList = resourceFileFinder.findFiles(path);
        
        for (File thisFile : fileList) {
            if (thisFile.isFile()) {
                LOG.fine("Reading " + resourceType + " ResourceEntity into cache: " + thisFile.getName());
                
                try {
                	IResourceHelper helper = ResourceHelperFactory.getResourceHelper(fhirVersion, resourceType);
                	ResourceMetadata newEntity = helper.getMetadataFromResource(thisFile);           
                	// Load into the main cache for profiles
	                ArrayList<SupportingArtefact> artefacts = processSupportingArtefacts(thisFile, resourceType);
	                // Sort artefacts by weight so they display in the correct order
	                Collections.sort(artefacts, new OrderByWeight());
	                newEntity.setArtefacts(artefacts);
	                
	                addToResourceList(newFileList,newEntity);
	                
	                addMessage("  - Loading " + resourceType + " resource with ID: " + newEntity.getResourceID() + " and version: " + newEntity.getVersionNo());

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
    
    private static HashMap<String, ExampleResources> cacheExamples(FHIRVersion fhirVersion){
    	
    	LOG.fine("Started loading example resources into cache");
		addMessage("Started loading example resources into cache");
    	
        // Now, read the resources into our cache
		HashMap<String, ExampleResources> examplesList = new HashMap<String, ExampleResources>();
        String path = EXAMPLES.getFilesystemPath(fhirVersion);
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
            	                    ResourceMetadata newEntity = new ResourceMetadata(thisFile.getName(), thisFile, EXAMPLES, false, null,
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
    
    private static ArrayList<SupportingArtefact> processSupportingArtefacts(File resourceFile, ResourceType resourceType) {
    	ArrayList<SupportingArtefact> artefacts = new ArrayList<>();
		
		String resourceFilename = FileLoader.removeFileExtension(resourceFile.getName());
		File dir = new File(resourceFile.getParent());
		File artefactDir = new File(dir.getAbsolutePath() + "/" + resourceFilename);
		LOG.fine("Looking for artefacts in directory:" + artefactDir.getAbsolutePath());
		
		if(artefactDir.exists() && artefactDir.isDirectory()) { 
			// Now, loop through and find any artefact files
            File[] fileList = artefactDir.listFiles();
            if (fileList != null) {
    	        for (File thisFile : fileList) {
    	        	// Add this to our list of artefacts (if we can identify what it is!
    	        	ArtefactType type = ArtefactType.getFromFilename(resourceType, thisFile.getName());
    	        	if (type != null) {
    	        		SupportingArtefact artefact = new SupportingArtefact(thisFile, type); 
    	        		artefacts.add(artefact);
    	        	}
    	        }
            }
		}
		return artefacts;
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
    
    public static ResourceMetadata getSingleResourceByID(FHIRVersion fhirVersion, String idPart, String versionPart) {
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
    
    public static ResourceEntityWithMultipleVersions getversionsByID(FHIRVersion fhirVersion, String idPart, String versionPart) {
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
    
    public static ResourceMetadata getSingleResourceByName(FHIRVersion fhirVersion, String name) {
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

	public static List<ResourceMetadata> getResourceList(FHIRVersion fhirVersion) {
		if(updateRequired()) {
            updateCache();
        }
		
		List<ResourceMetadata> latestResourcesList = new ArrayList<>();
		for (ResourceEntityWithMultipleVersions item : resourceListByFhirVersion.get(fhirVersion)) {
			latestResourcesList.add(item.getLatest());
		}
		return latestResourcesList;
	}
	
	public static ExampleResources getExamples(FHIRVersion fhirVersion, String resourceTypeAndID) {
		if(updateRequired()) {
            updateCache();
        }
		return examplesListByFhirVersion.get(fhirVersion).get(resourceTypeAndID);
	}
	
	public static ResourceMetadata getExampleByName(FHIRVersion fhirVersion, String resourceFilename) {
		if(updateRequired()) {
            updateCache();
        }
		return examplesListByName.get(fhirVersion).get(resourceFilename);
	}

    
    /*private static void printCacheContent(FHIRVersion fhirVersion) {
    	addMessage(" ===  ===  ===   Cache Contents for "+fhirVersion+"   === === ===");
    	LOG.fine("Cache loaded - entries:");
    	for (ResourceEntityWithMultipleVersions entry : resourceList.get(fhirVersion)) {
    		LOG.fine("  -> " + entry);
    		addMessage(entry.toString());
    	}
    	
    	LOG.fine("Examples:");
    	addMessage("Examples:");
    	for (String exampleOfProfile : examplesList.get(fhirVersion).keySet()) {
    		ExampleResources entries = examplesList.get(fhirVersion).get(exampleOfProfile);
    		addMessage("Examples for profile: " + exampleOfProfile);
    		for (ResourceEntity entry : entries) {
	    		LOG.fine("  -> " + entry);
	    		addMessage(entry.toString());
    		}
    	}
    	addMessage(" ===  ===  ===  End Cache Contents === === ===");
    }*/
}
