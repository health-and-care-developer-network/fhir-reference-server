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
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.datalayer.collections.ResourceEntityWithMultipleVersions;
import uk.nhs.fhir.load.FhirFileParser;
import uk.nhs.fhir.load.FhirParsingFailedException;
import uk.nhs.fhir.load.XmlFileFinder;
import uk.nhs.fhir.util.AbstractFhirFileLocator;
import uk.nhs.fhir.util.FHIRUtils;
import uk.nhs.fhir.util.FhirVersion;

/**
 * Holds an in-memory cache of metadata about FHIR resources loaded from the filesystem.
 *
 * @author Adam Hatherly
 */
public class FileCache {
    private static final Logger LOG = LoggerFactory.getLogger(FileCache.class.getName());

    private static final XmlFileFinder resourceFileFinder = new XmlFileFinder();
    private static final FhirFileParser parser = new FhirFileParser();
    
    private static AbstractFhirFileLocator fhirFileLocator = new PropertiesFhirFileLocator();
    private static final VersionedFilePreprocessor preprocessor = new VersionedFilePreprocessor(fhirFileLocator);
    public static void setVersionedFileLocator(AbstractFhirFileLocator versionedFileLocator) {
    	FileCache.fhirFileLocator = versionedFileLocator;
    	preprocessor.setFhirFileLocator(versionedFileLocator);
    }
    
    private static Object CACHE_COLLECTION_SYNCH_OBJECT = new Object();
    
    // Singleton object to act as a cache of the files in the profiles and valueset directories
    private static Map<FhirVersion, List<ResourceEntityWithMultipleVersions>> resourceListByFhirVersion = null;
    private static Map<FhirVersion, Map<String, List<ResourceMetadata>>> examplesListByFhirVersion = null;
    private static Map<FhirVersion, Map<String, ResourceMetadata>> examplesListByName = null;
    
    private static boolean cacheNeedsUpdating = true;
    
    public static void invalidateCache() {
    	cacheNeedsUpdating = true;
    	updateCache();
    }
    
    public static synchronized void clearCache() {
    	synchronized(CACHE_COLLECTION_SYNCH_OBJECT) {
	    	resourceListByFhirVersion = null;
	    	examplesListByFhirVersion = null;
	    	examplesListByName = null;
	    	invalidateCache();
    	}
    }
    
    private static boolean updateRequired() {
        if (cacheNeedsUpdating) {
            LOG.debug("Cache needs updating");
            return true;
        } else {
        	LOG.debug("Using Cache");
        	return false;
        }
    }
    
    private synchronized static void updateCache() {
        if(updateRequired()) {
        	for (FhirVersion version : FhirVersion.getSupportedVersions()) {
	        	updateCacheForSpecificFhirVersion(version);
        	}
        	cacheNeedsUpdating = false;
        }
    }
    
    private static void updateCacheForSpecificFhirVersion(FhirVersion fhirVersion) {
    	DataLoaderMessages.clearProfileLoadMessages();
        LOG.debug("Updating cache from filesystem");
        
        List<ResourceEntityWithMultipleVersions> newResourcesList = cacheResources(fhirVersion);
        HashMap<String, List<ResourceMetadata>> newExamplesList = cacheExamples(fhirVersion);
        
        updateResourcesList(fhirVersion, newResourcesList);
        updateExamplesList(fhirVersion, newExamplesList);
    }

	static List<ResourceEntityWithMultipleVersions> cacheResources(FhirVersion fhirVersion) {
		List<ResourceEntityWithMultipleVersions> newList = Lists.newArrayList();
        for (ResourceType resourceType : ResourceType.typesForFhirVersion(fhirVersion)) {
        	newList.addAll(cacheFHIRResources(fhirVersion, resourceType));
        }
		return newList;
	}
    
    private static List<ResourceEntityWithMultipleVersions> cacheFHIRResources(FhirVersion fhirVersion, ResourceType resourceType) {
    	
    	// Call pre-processor to copy files into the versioned directory
    	try {
    		preprocessor.copyFHIRResourcesIntoVersionedDirectory(fhirVersion, resourceType);
    	} catch (IOException e) {
    		LOG.error("Unable to pre-process files into versioned directory! - error: " + e.getMessage());
    	}
    	
    	LOG.debug("Started loading resources into cache");
		addMessage("Started loading " + resourceType + " resources into cache");
    	
        // Now, read the resources from the versioned path into our cache
    	List<ResourceEntityWithMultipleVersions> newFileList = Lists.newArrayList();
    	String sourcePathForResourceAndVersion = fhirFileLocator.getDestinationPathForResourceType(resourceType, fhirVersion).toString();
    	LOG.debug("Reading pre-processed files from path: " + sourcePathForResourceAndVersion);
        List<File> fileList = resourceFileFinder.findFiles(sourcePathForResourceAndVersion);
        
        for (File thisFile : fileList) {
            if (thisFile.isFile()) {
                LOG.debug("Reading " + resourceType + " ResourceEntity into cache: " + thisFile.getName());
                
                try {
                	IBaseResource parsedFile = parser.parseFile(thisFile);
                	if (FhirFileParser.isSupported(parsedFile)) {
						WrappedResource<?> wrappedResource = WrappedResource.fromBaseResource(parsedFile);
	                	
	                	if (wrappedResource.getImplicitFhirVersion().equals(fhirVersion)) {
	    					ResourceMetadata newEntity = wrappedResource.getMetadata(thisFile);
	    	                
	    	                addToResourceList(newFileList,newEntity);
	    	                
	    	                addMessage("  - Loading " + resourceType + " resource with ID: " + newEntity.getResourceID() + " and version: " + newEntity.getVersionNo());
	                	}
                	}

                } catch (Exception ex) {
                	LOG.error("Unable to load FHIR resource from file: "+thisFile.getAbsolutePath() + " - IGNORING");
                	addMessage("[!] Error loading " + resourceType + " resource from file : " + thisFile.getAbsolutePath() + " message: " + ex.getMessage());
                	ex.printStackTrace();
                }
            }
        }
        
        // Sort our collection into alpha order by resource name
        //Collections.sort(newFileList);
        LOG.debug("Finished reading resources into cache");
        return newFileList;
    }
    
    private static void addToResourceList(List<ResourceEntityWithMultipleVersions> list,
    										ResourceMetadata entry) {
    	boolean found = false;
    	for (ResourceEntityWithMultipleVersions listItem : list) {
    		if (listItem.getResourceID().equals(entry.getResourceID())
    		  && listItem.getResourceType().equals(entry.getResourceType())) {
    			// This is a new version of an existing resource - add the version
    			listItem.add(entry);
    			found = true;
    			LOG.debug("Added new version to resource: " + entry.getResourceID());
    		}
    	}
		if (!found) {
			// This is a new resource we haven't seen before
			ResourceEntityWithMultipleVersions newEntry = new ResourceEntityWithMultipleVersions(entry);
			list.add(newEntry);
			LOG.debug("Added new resource (first version found): " + entry.getResourceID());
		}
    }
    
    private static HashMap<String, List<ResourceMetadata>> cacheExamples(FhirVersion fhirVersion){
    	
    	// Call pre-processor to copy files into the versioned directory
        Path renderedExamplesPath = fhirFileLocator.getSourcePathForResourceType(EXAMPLES, fhirVersion);
        Path importedExamplesPath = fhirFileLocator.getDestinationPathForResourceType(EXAMPLES, fhirVersion);
        
        File importedExamplesPathFile = importedExamplesPath.toFile();
		if (!importedExamplesPathFile.exists() && !importedExamplesPathFile.mkdirs()) {
        	throw new IllegalStateException("Failed to create directory [" + importedExamplesPath.toString() + "]");
        }
        
		List<File> renderedFiles = resourceFileFinder.findFiles(renderedExamplesPath.toFile());
        
        for (File renderedFile : renderedFiles) {
        	String fileName = renderedFile.getName();
			try {
				FileUtils.copyFile(renderedFile, importedExamplesPath.resolve(fileName).toFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    	
    	LOG.debug("Started loading example resources into cache");
		addMessage("Started loading example resources into cache");
    	
        // Now, read the resources into our cache
		HashMap<String, List<ResourceMetadata>> examplesList = Maps.newHashMap();
        String path = fhirFileLocator.getDestinationPathForResourceType(EXAMPLES, fhirVersion).toString();
        List<File> fileList = resourceFileFinder.findFiles(path);
        
        for (File thisFile : fileList) {
            if (thisFile.isFile()) {
                LOG.debug("Reading example ResourceEntity into cache: " + thisFile.getName());
                
                String resourceID = null;
                
                try {
                	IBaseResource exampleResource = parser.parseFile(thisFile);
                	
                	// A supported file is any class we treat as part of a specification (StructureDefinition, ValueSet etc)
                	// Anything else we have successfully parsed, can be treated as an example
                	if (!FhirFileParser.isSupported(exampleResource)) {
                		resourceID = exampleResource.getIdElement().getIdPart();
                    
	                    // Find the profile resource ID the example relates to
	                    List<? extends IPrimitiveType<String>> profiles = exampleResource.getMeta().getProfile();
	                    if (profiles.isEmpty()) {
	                    	LOG.error("Unable to load FHIR example resource from file: "+thisFile.getAbsolutePath() + " - no profile was specified in the example!");
	                		addMessage("[!] Error loading example resource from file : " + thisFile.getAbsolutePath() + " no profile was specified in the example!");
	                    }
	                    for (IPrimitiveType<String> profile : profiles) {
	                    	String profileStr = profile.getValueAsString();
	                    	if (profileStr != null) {
	                    		if (profileStr.contains("_history")) {
	                    			LOG.error("Unable to load FHIR example resource from file: "+thisFile.getAbsolutePath() + " - versioned profile URLs not supported!");
	                    			addMessage("[!] Error loading example resource from file : " + thisFile.getAbsolutePath() + " versioned profile URLs not supported!");
	                    		} else {
	                    			String[] profileParts = profileStr.split("/");
	                    			if (profileParts.length < 3) {
	                    				LOG.error("Unable to load FHIR example resource from file: "+thisFile.getAbsolutePath() + " - profile URL invalid: " + profileStr);
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
	            	                    	List<ResourceMetadata> e = Lists.newArrayList();
	            	                    	e.add(newEntity);
	            	                    	examplesList.put(profileResourceID, e);
	            	                    }
	            	                    
	            		                addMessage("  - Loading example resource with ID: " + resourceID + " as an example of resource with ID: " + profileResourceID);
	                    			}
	                    		}
	                    	} else {
	                    		LOG.warn("Unable to load FHIR example resource from file: "+thisFile.getAbsolutePath() + " - no profile was specified in the example!");
	                    		addMessage("[!] Error loading example resource from file : " + thisFile.getAbsolutePath() + " no profile was specified in the example!");
	                    	}
	                    }
                	}
	                
                } catch (FhirParsingFailedException | RuntimeException ex) {
                	LOG.error("Unable to load FHIR example resource from file: "+thisFile.getAbsolutePath() + " - IGNORING");
                	addMessage("[!] Error loading example resource from file : " + thisFile.getAbsolutePath() + " message: " + ex.getMessage());
                }
            }
        }
        LOG.debug("Finished reading example resources into cache");
        return examplesList;
    }

	static void updateResourcesList(FhirVersion fhirVersion, List<ResourceEntityWithMultipleVersions> newResourcesList) {
		// Swap out for our new list
        ensureResourceListByFhirVersion();
        resourceListByFhirVersion.put(fhirVersion, newResourcesList);
	}

	static void updateExamplesList(FhirVersion fhirVersion, Map<String, List<ResourceMetadata>> newExamplesList) {
		ensureExamplesListByFhirVersionExists(fhirVersion);
        examplesListByFhirVersion.put(fhirVersion, newExamplesList);
        
        ensureExamplesListByNameExists();
        examplesListByName.put(fhirVersion, buildExampleListByName(newExamplesList));
	}

	static void ensureResourceListByFhirVersion() {
    	synchronized(CACHE_COLLECTION_SYNCH_OBJECT) {
			if (resourceListByFhirVersion == null) {
	        	resourceListByFhirVersion = Maps.newConcurrentMap();
	        }
    	}
	}

	static void ensureExamplesListByFhirVersionExists(FhirVersion fhirVersion) {
    	synchronized(CACHE_COLLECTION_SYNCH_OBJECT) {
			if (examplesListByFhirVersion == null) {
	        	examplesListByFhirVersion = Maps.newConcurrentMap();
	        }
    	}
	}

	static void ensureExamplesListByNameExists() {
    	synchronized(CACHE_COLLECTION_SYNCH_OBJECT) {
			if (examplesListByName == null) {
	        	examplesListByName = Maps.newConcurrentMap();
	        }
    	}
	}
    
    /**
     * Takes the in-memory index used for finding examples for specific profile IDs and flips it to
     * return an index keyed on the example filename
     * @param oldList
     * @return
     */
    private static Map<String, ResourceMetadata> buildExampleListByName(Map<String, List<ResourceMetadata>> oldList) {
    	return 
    		oldList
    			.values()
    			.stream()
    			.flatMap(metadatas -> metadatas.stream())
    			.collect(Collectors.toConcurrentMap(
    				example -> example.getResourceName(),
    				example -> example));
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
        List<String> nameList = Lists.newArrayList(names);
        return nameList;
    }
    
    /**
     * Get a list of all extensions to show in the extensions registry
     * @return
     */
    public static List<ResourceMetadata> getExtensions(FhirVersion fhirVersion)  {
    	List<ResourceMetadata> results = Lists.newArrayList();
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
        
        LOG.debug("Creating HashMap");
        HashMap<String, List<ResourceMetadata>> result = new HashMap<String, List<ResourceMetadata>>();
        try {
            for (FhirVersion fhirVersion : FhirVersion.getSupportedVersions()) {
	        	for(ResourceEntityWithMultipleVersions entry : resourceListByFhirVersion.get(fhirVersion)) {
	            	if (entry.getLatest().getResourceType().equals(resourceType)) {
		            	boolean isExtension = entry.getLatest().isExtension();
		                String group = entry.getLatest().getDisplayGroup();
		                
		                // Don't include extensions
		                if (!isExtension) {
			                if(result.containsKey(group)) {
			                    List<ResourceMetadata> resultEntry = result.get(group);
			                    resultEntry.add(entry.getLatest());
			                } else {
			                    List<ResourceMetadata> resultEntry = Lists.newArrayList();
			                    resultEntry.add(entry.getLatest());
			                    result.put(group, resultEntry);
			                }
		                }
	            	}
	            }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("ERROR: " + e.getMessage());
        }
        LOG.debug("Generated HashMap: " + result.toString());
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
        List<IBaseResource> allFiles = Lists.newArrayList();
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
    
    public static ResourceMetadata getSingleResourceByID(FhirVersion fhirVersion, IIdType type) {
        if(updateRequired()) {
            updateCache();
        }
        
    	for (ResourceEntityWithMultipleVersions entry : resourceListByFhirVersion.get(fhirVersion)) {
    		if (entry.getResourceID().equals(type.getIdPart())
    		  && entry.getResourceType().equals(ResourceType.getTypeFromHAPIName(type.getResourceType()))) {
    			if (type.getVersionIdPart() != null) {
    				// Get a specific version
    				VersionNumber version = new VersionNumber(type.getVersionIdPart());
    				LOG.debug("Getting versioned resource with ID=" + type.getIdPart() + ", type=" + type.getResourceType() + ", version=" + version);
    				return entry.getSpecificVersion(version);
    			} else {
    				// Get the latest
    				return entry.getLatest();
    			}
    		}
    	}
    	return null;
    }
    
    public static ResourceEntityWithMultipleVersions getversionsByID(FhirVersion fhirVersion, String idPart, ResourceType resourceType) {
        if(updateRequired()) {
            updateCache();
        }
        
    	for (ResourceEntityWithMultipleVersions entry : resourceListByFhirVersion.get(fhirVersion)) {
    		if (entry.getResourceID().equals(idPart)
    		  && entry.getResourceType().equals(resourceType)) {
    			return entry;
    		}
    	}
    	return null;
    }
    
    public static ResourceMetadata getSingleResourceByName(FhirVersion fhirVersion, String name, ResourceType resourceType) {
        if(updateRequired()) {
            updateCache();
        }
    	for (ResourceEntityWithMultipleVersions entry : resourceListByFhirVersion.get(fhirVersion)) {
    		if (entry.getResourceName().equals(name)
    		  && entry.getResourceType().equals(resourceType)) {
    			return entry.getLatest();
    		}
    	}
    	return null;
    }

	public static List<ResourceMetadata> getResourceList(FhirVersion fhirVersion) {
		if(updateRequired()) {
            updateCache();
        }
		
		List<ResourceMetadata> latestResourcesList = Lists.newArrayList();
		for (ResourceEntityWithMultipleVersions item : resourceListByFhirVersion.get(fhirVersion)) {
			latestResourcesList.add(item.getLatest());
		}
		return latestResourcesList;
	}
	
	public static List<ResourceMetadata> getExamples(FhirVersion fhirVersion, String resourceTypeAndID) {
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
