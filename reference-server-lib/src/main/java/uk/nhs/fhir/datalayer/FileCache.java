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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.datalayer.collections.ResourceEntityWithMultipleVersions;
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
    
    // Allows cache update to be performed atomically (i.e. once started replacing cached data collections, 
    // no data can be requested until the replacement is finished). Not bomb-proof as a request could technically 
    // get something before and something else after replacement.
    private static final Object CACHE_SYNCH_OBJ = new Object();
    
    private static AbstractFhirFileLocator fhirFileLocator = new PropertiesFhirFileLocator();
    public static void setVersionedFileLocator(AbstractFhirFileLocator versionedFileLocator) {
    	FileCache.fhirFileLocator = versionedFileLocator;
    }
    
    // Singleton object to act as a cache of the files in the profiles and valueset directories
    private static Map<FhirVersion, List<ResourceEntityWithMultipleVersions>> resourceListByFhirVersion = Maps.newConcurrentMap();
    private static Map<FhirVersion, Map<String, List<ResourceMetadata>>> examplesListByFhirVersion = Maps.newConcurrentMap();
    private static Map<FhirVersion, Map<String, ResourceMetadata>> examplesListByName = Maps.newConcurrentMap();
    
    private static AtomicBoolean updatingCache = new AtomicBoolean(false);
    
    private static final Runnable UPDATE_CACHE =
    	new Runnable() {
			public void run() {
				try {
			    	DataLoaderMessages.clearProfileLoadMessages();
			        LOG.debug("Updating cache from filesystem");
			        
					FileCacher fileCacher = new FileCacher(fhirFileLocator);
					fileCacher.snapshotResourceMetadata();
					
					LOG.info("Finished caching resources. Updating cached resource maps.");
					
					synchronized (CACHE_SYNCH_OBJ) {
						resourceListByFhirVersion = fileCacher.getResourceListByFhirVersion();
						examplesListByFhirVersion = fileCacher.getExamplesListByFhirVersion();
						examplesListByName = fileCacher.getExamplesListByName();
					}
				} finally {
					updatingCache.set(false);
				}
			}
		};

	// If a refresh is not already running, kick it off now in a new thread  
    public static void invalidateCache() {
		if (updatingCache.compareAndSet(false, true)) {
			LOG.info("Invalidating cache on thread " + Thread.currentThread().getName());
			UPDATE_CACHE.run();
		} else {
			LOG.info("Would have triggered cache update, but it was already running (updatingCache is true)");
		}
    }
    
    /**
     * Method to get the cached set of Resource names of the specified type
     * 
     * @return 
     */
    public static List<String> getResourceNameList(FhirVersion fhirVersion, ResourceType resourceType) {
        HashSet<String> names = new HashSet<String>();
        for (ResourceEntityWithMultipleVersions entry : resourcesForFhirVersion(fhirVersion)) {
        	if (entry.getLatest().getResourceType() == resourceType
        	  && !names.contains(entry.getLatest().getResourceName())) {
    			names.add(entry.getLatest().getResourceName());
        	}
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
    	
		for(ResourceEntityWithMultipleVersions entry : resourcesForFhirVersion(fhirVersion)) {
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
        LOG.debug("Creating HashMap");
        HashMap<String, List<ResourceMetadata>> result = new HashMap<String, List<ResourceMetadata>>();
        try {
            for (FhirVersion fhirVersion : FhirVersion.getSupportedVersions()) {
	        	for(ResourceEntityWithMultipleVersions entry : resourcesForFhirVersion(fhirVersion)) {
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
        // Load each resource file and put them in a list to return
        int counter = 0;
        List<IBaseResource> allFiles = Lists.newArrayList();
        for (ResourceEntityWithMultipleVersions entry : resourcesForFhirVersion(fhirVersion)) {
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
    	for (ResourceEntityWithMultipleVersions entry : resourcesForFhirVersion(fhirVersion)) {
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
    	for (ResourceEntityWithMultipleVersions entry : resourcesForFhirVersion(fhirVersion)) {
    		if (entry.getResourceID().equals(idPart)
    		  && entry.getResourceType().equals(resourceType)) {
    			return entry;
    		}
    	}
        
    	return null;
    }
    
    public static ResourceMetadata getSingleResourceByName(FhirVersion fhirVersion, String name, ResourceType resourceType) {
        for (ResourceEntityWithMultipleVersions entry : resourcesForFhirVersion(fhirVersion)) {
        	if (entry != null
        	  && entry.getResourceName().equals(name)
    		  && entry.getResourceType().equals(resourceType)) {
    			return entry.getLatest();
    		}
    	}
        
    	return null;
    }

	public static List<ResourceMetadata> getResourceList(FhirVersion fhirVersion) {
		List<ResourceMetadata> latestResourcesList = Lists.newArrayList();
		
		for (ResourceEntityWithMultipleVersions item : resourcesForFhirVersion(fhirVersion)) {
			if (item != null) {
				latestResourcesList.add(item.getLatest());
			}
		}
		
		return latestResourcesList;
	}
    
    private static List<ResourceEntityWithMultipleVersions> resourcesForFhirVersion(FhirVersion fhirVersion) {
    	synchronized (CACHE_SYNCH_OBJ) {
    		return Lists.newArrayList(resourceListByFhirVersion.getOrDefault(fhirVersion, Lists.newArrayList()));
    	}
    }
	
	public static List<ResourceMetadata> getExamples(FhirVersion fhirVersion, String resourceTypeAndID) {
		synchronized (CACHE_SYNCH_OBJ) {
			return Lists.newArrayList(examplesListByFhirVersion.getOrDefault(fhirVersion, Maps.newHashMap()).getOrDefault(resourceTypeAndID, Lists.newArrayList()));
		}
	}
	
	public static Optional<ResourceMetadata> getExampleByName(FhirVersion fhirVersion, String resourceFilename) {
		synchronized (CACHE_SYNCH_OBJ) {
			return Optional.ofNullable(examplesListByName.getOrDefault(fhirVersion, Maps.newHashMap()).get(resourceFilename));
		}
	}
}
