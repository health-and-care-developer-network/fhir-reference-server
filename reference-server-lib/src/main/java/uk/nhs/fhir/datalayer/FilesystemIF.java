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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import ca.uhn.fhir.model.primitive.IdDt;
import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.datalayer.collections.ResourceEntityWithMultipleVersions;
import uk.nhs.fhir.util.FHIRUtils;
import uk.nhs.fhir.util.FhirVersion;

/**
 *
 * @author Tim Coates
 */
public class FilesystemIF {
    private static final Logger LOG = LoggerFactory.getLogger(FilesystemIF.class.getName());

    public FilesystemIF() {
        LOG.info("FilesystemIF instantiated, using filesystem datasource.");
    }
    
    public static void invalidateCache() {
    	FileCache.invalidateCache();
    }
    
    /**
     * Gets a specific resource, optionally also with a specific version
     * @param id
     * @return 
     */
    public IBaseResource getResourceByID(FhirVersion fhirVersion, IIdType theId) {
    	ResourceMetadata entry = FileCache.getSingleResourceByID(fhirVersion, theId);
    	if (entry != null) {
	    	File path = entry.getResourceFile();
	    	LOG.debug("Getting Resource with id=" + theId.getIdPart() + " looking for file: " + path.getAbsolutePath());
	        
	    	IBaseResource foundResource = FHIRUtils.loadResourceFromFile(fhirVersion, path);
	        return foundResource;
    	} else {
    		return null;
    	}
    }
    
    public ResourceMetadata getResourceEntityByID(FhirVersion fhirVersion, IIdType theId) {
    	String idPart = theId.getIdPart();
		ResourceType resourceType = ResourceType.getTypeFromHAPIName(theId.getResourceType());
		ResourceEntityWithMultipleVersions versionsByID = FileCache.getversionsByID(fhirVersion, idPart, resourceType);
		
		if (versionsByID == null) {
			return null;
		}
		
		if (theId.hasVersionIdPart()) {
    		VersionNumber version = new VersionNumber(theId.getVersionIdPart());
    		return versionsByID.getSpecificVersion(version);
    	} else {
    		return versionsByID.getLatest();
    	}
    	
    }
    
    public ResourceEntityWithMultipleVersions getVersionsByID(FhirVersion fhirVersion, IIdType theId) {
    	return FileCache.getversionsByID(fhirVersion, theId.getIdPart(), ResourceType.getTypeFromHAPIName(theId.getResourceType()));
    }

    /**
     * Gets a specific one, with no version specified (i.e. get the latest)
     * @param id
     * @return 
     */
    public IBaseResource getResourceByID(FhirVersion fhirVersion, String id) {
    	return getResourceByID(fhirVersion, new IdDt(id));
    }
    
    /**
     * This is the method to do a search based on name, ie to find where
     * name:contains=[parameter]
     * 
     * @param theNamePart
     * @return 
     */
    public List<IBaseResource> getResourceMatchByName(FhirVersion fhirVersion, ResourceType resourceType,
    				String theNamePart, int theFromIndex, int theToIndex) {
        LOG.info("Getting " + resourceType.name() + " resources with name containing: " + theNamePart);
        
        List<IBaseResource> list = Lists.newArrayList();
        List<ResourceMetadata> matchingIDs = getAllResourceIDforResourcesMatchingNamePattern(fhirVersion, resourceType, theNamePart);

        int counter = 0;
        for(ResourceMetadata entity : matchingIDs) {
        	if (counter >= theFromIndex && counter < theToIndex) {
        		list.add(getResourceByID(fhirVersion, entity.getResourceID()));
        	}
        	counter++;
        }
        return list;
    }

    /**
     * This is the method to count the number of matches based on name, ie to find where
     * name:contains=[parameter]
     * 
     * @param theNamePart
     * @return 
     */
    public int getResourceCountByName(FhirVersion fhirVersion, ResourceType resourceType, String theNamePart) {
        LOG.info("Getting the count of " + resourceType.name() + " resources with name containing: " + theNamePart);
        List<ResourceMetadata> matchingIDs = getAllResourceIDforResourcesMatchingNamePattern(fhirVersion, resourceType, theNamePart);
        return matchingIDs.size();
    }

	public List<IBaseResource> getResourceMatchByURL(FhirVersion fhirVersion, ResourceType resourceType, String theURL,
															int theFromIndex, int theToIndex) {
		List<ResourceMetadata> resourceList = FileCache.getResourceList(fhirVersion);
        List<IBaseResource> matches = Lists.newArrayList();
        int counter = 0;
        for (ResourceMetadata entry : resourceList) {
        	if (entry.getUrl().equals(theURL) && entry.getResourceType().equals(resourceType)) {
        		if (counter >= theFromIndex && counter < theToIndex) {
        			matches.add(getResourceByID(fhirVersion, entry.getResourceID()));
        		}
        		counter++;
        	}
        }		
		return matches;
	}

	public int getResourceCountByURL(FhirVersion fhirVersion, ResourceType resourceType, String theURL) {
		List<ResourceMetadata> resourceList = FileCache.getResourceList(fhirVersion);
        int counter = 0;
        for (ResourceMetadata entry : resourceList) {
        	if (entry.getUrl().equals(theURL) && entry.getResourceType().equals(resourceType)) {
        		counter++;
        	}
        }		
		return counter;
	}
	
    /**
     * Gets a full list of StructureDefinition objects. Not especially performant.
     * 
     * @return 
     */
    public List<IBaseResource> getAllResourcesOfType(FhirVersion fhirVersion, ResourceType resourceType,
    													int theFromIndex, int theToIndex) {
        LOG.info("Getting all resources of type: " + resourceType.name());
        return FileCache.getResources(fhirVersion, resourceType, theFromIndex, theToIndex);
    }
    
    /**
     * Gets a full list of names for the web view of /StructureDefinition requests.
     * 
     * @return 
     */
    public List<String> getAllResourceNames(FhirVersion fhirVersion, ResourceType resourceType) {
        LOG.info("Getting all Resource Names for type: " + resourceType.name());
        return FileCache.getResourceNameList(fhirVersion, resourceType);
    }
    
    /**
     * Get a list of all extensions to show in the extensions registry
     * @return
     */
    public List<ResourceMetadata> getExtensions()  {
    	LOG.info("Getting all Extensions");
        
    	List<ResourceMetadata> result = Lists.newArrayList();
    	
    	for (FhirVersion fhirVersion : FhirVersion.getSupportedVersions()) {
    		result.addAll(FileCache.getExtensions(fhirVersion));
    	}
    	
    	return result;
    }
    
    /**
     * Gets a full list of names grouped by base resource for the web view 
     * of /StructureDefinition requests.
     * 
     * @return 
     */
    public HashMap<String, List<ResourceMetadata>> getAllResourceNamesByBaseResource(ResourceType resourceType) {
        LOG.info("Getting all Resource Names by base resource");
        return FileCache.getGroupedNameList(resourceType);
    }
    
    /**
     * Gets a full list of resource names grouped by the broad category of the resource
     * for the web view of /[ResourceType] requests.
     */
	public HashMap<String, List<ResourceMetadata>> getAllResourceNamesByCategory(ResourceType resourceType) {
    	LOG.info("Getting all Resource Names by category");
        return FileCache.getGroupedNameList(resourceType);
	}
    

    /**
     * This is the method to search by name, e.g. name:contains=Patient
     * 
     * @param theNamePart
     * @return a list of IDs of matching resources
     */
    public List<ResourceMetadata> getAllResourceIDforResourcesMatchingNamePattern(FhirVersion fhirVersion, ResourceType resourceType, String theNamePart) {
        LOG.info("Getting all StructureDefinition Names containing: " + theNamePart + " in their name");
        
        // Get full list of profiles first
        List<ResourceMetadata> resourceList = FileCache.getResourceList(fhirVersion);
        
        // Now filter the list to those matching our criteria
        List<ResourceMetadata> matches = Lists.newArrayList();
        
        String pattern = "(.*)" + theNamePart + "(.*)";
        
        for (ResourceMetadata entry : resourceList) {
        	if (entry.getResourceType().equals(resourceType)) {
	        	String resourceName = entry.getResourceName();
	        	// Create a Pattern object
	            Pattern r = Pattern.compile(pattern);
	            // Now create matcher object.
	            Matcher m = r.matcher(resourceName);
	            if (m.find()) {
	               matches.add(entry);
	            }
        	}
        }
        LOG.debug("Returning matches");
        return matches;
    }
    
	public List<ResourceMetadata> getExamples(FhirVersion fhirVersion, String resourceTypeAndID) {
		return FileCache.getExamples(fhirVersion, resourceTypeAndID);
	}
	
	public Optional<ResourceMetadata> getExampleByName(FhirVersion fhirVersion, String resourceFilename) {
		return FileCache.getExampleByName(fhirVersion, resourceFilename);
	}

	public HashMap<String, Integer> getResourceTypeCounts() {
		HashMap<String, Integer> results = new HashMap<String, Integer>();
		for (FhirVersion fhirVersion : FhirVersion.getSupportedVersions()) {
			List<ResourceMetadata> list = FileCache.getResourceList(fhirVersion);
			for (ResourceMetadata entry : list) {
				String type = entry.getResourceType().toString();
				if (entry.isExtension()) {
					type = "Extension";
				}
				if (results.containsKey(type)) {
					Integer i = results.get(type);
					results.put(type, i + 1);
				} else {
					results.put(type, 1);
				}
			}
		}
		return results;
	}
	
	public int getResourceCount(FhirVersion fhirVersion, ResourceType resourceType) {
		int count = 0;
		List<ResourceMetadata> list = FileCache.getResourceList(fhirVersion);
		for (ResourceMetadata entry : list) {
			ResourceType type = entry.getResourceType();
			if (type == resourceType) {
				count++;
			}
		}
		return count;
	}
}
