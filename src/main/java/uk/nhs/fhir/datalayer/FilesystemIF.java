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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.primitive.IdDt;
import uk.nhs.fhir.datalayer.collections.ExampleResources;
import uk.nhs.fhir.datalayer.collections.ResourceEntity;
import uk.nhs.fhir.datalayer.collections.ResourceEntityWithMultipleVersions;
import uk.nhs.fhir.datalayer.collections.VersionNumber;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.util.FHIRUtils;
import uk.nhs.fhir.util.FHIRVersion;
import uk.nhs.fhir.util.FhirServerProperties;

/**
 *
 * @author Tim Coates
 */
public class FilesystemIF {
    private static final Logger LOG = Logger.getLogger(FilesystemIF.class.getName());

    private static String logLevel = FhirServerProperties.getProperty("logLevel");

    /**
     * Constructor, we simply set the logging level, and log that we've been instantiated.
     * 
     */
    public FilesystemIF() {
        LOG.setLevel(Level.INFO);

        if(logLevel.equals("FINE")) {
            LOG.setLevel(Level.FINE);
        }
        if(logLevel.equals("OFF")) {
            LOG.setLevel(Level.OFF);
        }
        LOG.info("FilesystemIF instantiated, using filesystem datasource.");
    }

    /**
     * Gets a specific one, optionally also with a specific version (DSTU2 variant)
     * @param id
     * @return 
     */
    public IBaseResource getResourceByID(FHIRVersion fhirVersion, IdDt theId) {
    	ResourceEntity entry = FileCache.getSingleResourceByID(fhirVersion, theId.getIdPart(), theId.getVersionIdPart());
    	if (entry != null) {
	    	File path = entry.getResourceFile();
	    	LOG.fine("Getting Resource with id=" + theId.getIdPart() + " looking for file: " + path.getAbsolutePath());
	        
	    	IBaseResource foundResource = FHIRUtils.loadResourceFromFile(fhirVersion, path);
	        return foundResource;
    	} else {
    		return null;
    	}
    }
    
    /**
     * Gets a specific one, optionally also with a specific version
     * @param id
     * @return 
     */
    public IBaseResource getResourceByID(FHIRVersion fhirVersion, IdType theId) {
    	ResourceEntity entry = FileCache.getSingleResourceByID(fhirVersion, theId.getIdPart(), theId.getVersionIdPart());
    	if (entry != null) {
	    	File path = entry.getResourceFile();
	    	LOG.info("Getting Resource with id=" + theId.getIdPart() + " looking for file: " + path.getAbsolutePath());
	        
	    	IBaseResource foundResource = FHIRUtils.loadResourceFromFile(fhirVersion, path);
	        return foundResource;
    	} else {
    		return null;
    	}
    }
    
    public ResourceEntity getResourceEntityByID(FHIRVersion fhirVersion, IdDt theId) {
    	if (theId.hasVersionIdPart()) {
    		VersionNumber version = new VersionNumber(theId.getVersionIdPart());
    		return FileCache.getversionsByID(fhirVersion, theId.getIdPart(), theId.getVersionIdPart()).getSpecificVersion(version);
    	} else {
    		return FileCache.getversionsByID(fhirVersion, theId.getIdPart(), theId.getVersionIdPart()).getLatest();
    	}
    	
    }
    
    public ResourceEntity getResourceEntityByID(FHIRVersion fhirVersion, IdType theId) {
    	if (theId.hasVersionIdPart()) {
    		VersionNumber version = new VersionNumber(theId.getVersionIdPart());
    		return FileCache.getversionsByID(fhirVersion, theId.getIdPart(), theId.getVersionIdPart()).getSpecificVersion(version);
    	} else {
    		return FileCache.getversionsByID(fhirVersion, theId.getIdPart(), theId.getVersionIdPart()).getLatest();
    	}
    	
    }
    
    public ResourceEntityWithMultipleVersions getVersionsByID(FHIRVersion fhirVersion, IdDt theId) {
    	return FileCache.getversionsByID(fhirVersion, theId.getIdPart(), theId.getVersionIdPart());
    }
    
    public ResourceEntityWithMultipleVersions getVersionsByID(FHIRVersion fhirVersion, IdType theId) {
    	return FileCache.getversionsByID(fhirVersion, theId.getIdPart(), theId.getVersionIdPart());
    }

    /**
     * Gets a specific one, with no version specified (i.e. get the latest)
     * @param id
     * @return 
     */
    public IBaseResource getResourceByID(FHIRVersion fhirVersion, String id) {
    	return getResourceByID(fhirVersion, new IdDt(id));
    }
    
    /**
     * This is the method to do a search based on name, ie to find where
     * name:contains=[parameter]
     * 
     * @param theNamePart
     * @return 
     */
    public List<IBaseResource> getResourceMatchByName(FHIRVersion fhirVersion, ResourceType resourceType,
    				String theNamePart, int theFromIndex, int theToIndex) {
        LOG.info("Getting " + resourceType.name() + " resources with name containing: " + theNamePart);
        
        List<IBaseResource> list = new ArrayList<IBaseResource>();
        List<ResourceEntity> matchingIDs = getAllResourceIDforResourcesMatchingNamePattern(fhirVersion, resourceType, theNamePart);

        int counter = 0;
        for(ResourceEntity entity : matchingIDs) {
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
    public int getResourceCountByName(FHIRVersion fhirVersion, ResourceType resourceType, String theNamePart) {
        LOG.info("Getting the count of " + resourceType.name() + " resources with name containing: " + theNamePart);
        List<ResourceEntity> matchingIDs = getAllResourceIDforResourcesMatchingNamePattern(fhirVersion, resourceType, theNamePart);
        return matchingIDs.size();
    }

	public List<IBaseResource> getResourceMatchByURL(FHIRVersion fhirVersion, ResourceType resourceType, String theURL,
															int theFromIndex, int theToIndex) {
		List<ResourceEntity> resourceList = FileCache.getResourceList(fhirVersion);
        ArrayList<IBaseResource> matches = new ArrayList<IBaseResource>();
        int counter = 0;
        for (ResourceEntity entry : resourceList) {
        	if (entry.getUrl().equals(theURL) && entry.getResourceType().equals(resourceType)) {
        		if (counter >= theFromIndex && counter < theToIndex) {
        			matches.add(getResourceByID(fhirVersion, entry.getResourceID()));
        		}
        		counter++;
        	}
        }		
		return matches;
	}

	public int getResourceCountByURL(FHIRVersion fhirVersion, ResourceType resourceType, String theURL) {
		List<ResourceEntity> resourceList = FileCache.getResourceList(fhirVersion);
        int counter = 0;
        for (ResourceEntity entry : resourceList) {
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
    public List<IBaseResource> getAllResourcesOfType(FHIRVersion fhirVersion, ResourceType resourceType,
    													int theFromIndex, int theToIndex) {
        LOG.info("Getting all resources of type: " + resourceType.name());
        return FileCache.getResources(fhirVersion, resourceType, theFromIndex, theToIndex);
    }
    
    /**
     * Gets a full list of names for the web view of /StructureDefinition requests.
     * 
     * @return 
     */
    public List<String> getAllResourceNames(FHIRVersion fhirVersion, ResourceType resourceType) {
        LOG.info("Getting all Resource Names for type: " + resourceType.name());
        return FileCache.getResourceNameList(fhirVersion, resourceType);
    }
    
    /**
     * Get a list of all extensions to show in the extensions registry
     * @return
     */
    public List<ResourceEntity> getExtensions()  {
    	LOG.info("Getting all Extensions");
        
    	List<ResourceEntity> result = new ArrayList<ResourceEntity>();
    	
    	for (FHIRVersion fhirVersion : FHIRVersion.values()) {
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
    public HashMap<String, List<ResourceEntity>> getAllResourceNamesByBaseResource(ResourceType resourceType) {
        LOG.info("Getting all Resource Names by base resource");
        return FileCache.getGroupedNameList(resourceType);
    }
    
    /**
     * Gets a full list of resource names grouped by the broad category of the resource
     * for the web view of /[ResourceType] requests.
     */
	public HashMap<String, List<ResourceEntity>> getAllResourceNamesByCategory(ResourceType resourceType) {
    	LOG.info("Getting all Resource Names by category");
        return FileCache.getGroupedNameList(resourceType);
	}
    

    /**
     * This is the method to search by name, e.g. name:contains=Patient
     * 
     * @param theNamePart
     * @return a list of IDs of matching resources
     */
    public List<ResourceEntity> getAllResourceIDforResourcesMatchingNamePattern(FHIRVersion fhirVersion, ResourceType resourceType, String theNamePart) {
        LOG.info("Getting all StructureDefinition Names containing: " + theNamePart + " in their name");
        
        // Get full list of profiles first
        List<ResourceEntity> resourceList = FileCache.getResourceList(fhirVersion);
        
        // Now filter the list to those matching our criteria
        ArrayList<ResourceEntity> matches = new ArrayList<ResourceEntity>();
        
        String pattern = "(.*)" + theNamePart + "(.*)";
        
        for (ResourceEntity entry : resourceList) {
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
        LOG.fine("Returning matches");
        return matches;
    }
    
	public ExampleResources getExamples(FHIRVersion fhirVersion, String resourceTypeAndID) {
		return FileCache.getExamples(fhirVersion, resourceTypeAndID);
	}
	
	public ResourceEntity getExampleByName(FHIRVersion fhirVersion, String resourceFilename) {
		return FileCache.getExampleByName(fhirVersion, resourceFilename);
	}

	public HashMap<String, Integer> getResourceTypeCounts() {
		HashMap<String, Integer> results = new HashMap<String, Integer>();
		for (FHIRVersion fhirVersion : FHIRVersion.values()) {
			List<ResourceEntity> list = FileCache.getResourceList(fhirVersion);
			for (ResourceEntity entry : list) {
				String type = entry.getResourceType().toString();
				if (entry.isExtension()) {
					type = "Extension";
				}
				if (results.containsKey(type)) {
					Integer i = results.get(type);
					results.put(type, i + 1);
				} else {
					results.put(type, new Integer(1));
				}
			}
		}
		return results;
	}
	
	public int getResourceCount(FHIRVersion fhirVersion, ResourceType resourceType) {
		int count = 0;
		List<ResourceEntity> list = FileCache.getResourceList(fhirVersion);
		for (ResourceEntity entry : list) {
			ResourceType type = entry.getResourceType();
			if (type == resourceType) {
				count++;
			}
		}
		return count;
	}
}
